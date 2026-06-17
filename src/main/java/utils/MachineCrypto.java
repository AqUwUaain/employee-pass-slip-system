package utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.prefs.Preferences;

/**
 * Machine-specific encryption for storing sensitive data (e.g. Remember Me password).
 * The encryption key is derived from hostname + username, so encrypted values
 * cannot be decrypted on a different machine or by a different user.
 */
public final class MachineCrypto {

    private MachineCrypto() {
    }

    private static final String PREF_KEY = "machine_enc_key_v1";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private static final Preferences prefs =
            Preferences.userNodeForPackage(MachineCrypto.class);

    /**
     * Returns a machine+user-specific AES key.
     * Generated once, then stored in Java Preferences for consistency.
     */
    private static SecretKeySpec getOrCreateKey() {
        byte[] keyBytes = prefs.getByteArray(PREF_KEY, null);

        if (keyBytes == null) {
            try {
                KeyGenerator gen = KeyGenerator.getInstance("AES");
                gen.init(256);
                SecretKey key = gen.generateKey();
                keyBytes = key.getEncoded();
                prefs.putByteArray(PREF_KEY, keyBytes);
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate AES key", e);
            }
        }

        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Derives a 32-byte seed from the current machine hostname + OS username.
     * Used as additional entropy mixed into the key so encrypted data is
     * tied to this specific machine and user account.
     */
    private static byte[] getMachineSeed() {
        try {
            String hostname = java.net.InetAddress.getLocalHost().getHostName();
            String username = System.getProperty("user.name", "unknown");
            String raw = hostname + "|" + username;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(raw.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return "fallback-seed".getBytes(StandardCharsets.UTF_8);
        }
    }

    /**
     * Encrypts a plaintext string using AES-GCM with a machine-specific key.
     * Returns a Base64-encoded string that can be safely stored.
     */
    public static String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) return "";

        try {
            SecretKeySpec baseKey = getOrCreateKey();
            byte[] machineSeed = getMachineSeed();

            // Mix machine seed into the key for machine-binding
            byte[] keyBytes = baseKey.getEncoded();
            for (int i = 0; i < keyBytes.length && i < machineSeed.length; i++) {
                keyBytes[i] ^= machineSeed[i];
            }
            SecretKeySpec mixedKey = new SecretKeySpec(keyBytes, "AES");

            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, mixedKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Prepend IV to ciphertext so we can extract it during decryption
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Decrypts a Base64-encoded encrypted string back to plaintext.
     * Returns empty string if decryption fails (e.g. wrong machine).
     */
    public static String decrypt(String encryptedBase64) {
        if (encryptedBase64 == null || encryptedBase64.isEmpty()) return "";

        try {
            byte[] combined = Base64.getDecoder().decode(encryptedBase64);

            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encrypted = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            System.arraycopy(combined, iv.length, encrypted, 0, encrypted.length);

            SecretKeySpec baseKey = getOrCreateKey();
            byte[] machineSeed = getMachineSeed();
            byte[] keyBytes = baseKey.getEncoded();
            for (int i = 0; i < keyBytes.length && i < machineSeed.length; i++) {
                keyBytes[i] ^= machineSeed[i];
            }
            SecretKeySpec mixedKey = new SecretKeySpec(keyBytes, "AES");

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, mixedKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }
}
