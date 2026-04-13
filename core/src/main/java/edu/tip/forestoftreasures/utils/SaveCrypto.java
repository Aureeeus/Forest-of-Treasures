package edu.tip.forestoftreasures.utils;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Handles AES-256-CBC encryption/decryption and HMAC-SHA256 integrity
 * verification for save data. The save pipeline is:
 * <pre>
 *   JSON string → AES-256-CBC encrypt → prepend IV → HMAC → Base64 → file
 * </pre>
 * The load pipeline reverses this and rejects tampered files.
 */
public final class SaveCrypto {
  private static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";
  private static final String HMAC_ALGORITHM = "HmacSHA256";
  private static final int IV_LENGTH = 16;
  private static final int HMAC_LENGTH = 32;

  // 256-bit AES key (32 bytes). Hardcoded for single-player anti-cheat.
  private static final byte[] AES_KEY = {
    0x4F, 0x72, 0x65, 0x73, 0x74, 0x5F, 0x54, 0x72,
    0x65, 0x61, 0x73, 0x75, 0x72, 0x65, 0x73, 0x21,
    0x53, 0x61, 0x76, 0x65, 0x4B, 0x65, 0x79, 0x32,
    0x30, 0x32, 0x36, 0x53, 0x65, 0x63, 0x72, 0x74
  };

  // Separate 256-bit HMAC key for tamper detection
  private static final byte[] HMAC_KEY = {
    0x48, 0x4D, 0x41, 0x43, 0x5F, 0x46, 0x6F, 0x72,
    0x65, 0x73, 0x74, 0x5F, 0x4F, 0x66, 0x5F, 0x54,
    0x72, 0x65, 0x61, 0x73, 0x75, 0x72, 0x65, 0x73,
    0x5F, 0x49, 0x6E, 0x74, 0x65, 0x67, 0x72, 0x69
  };

  private SaveCrypto() {
    throw new InstantiationError("Utility class cannot be instantiated.");
  }

  /**
   * Encrypts a JSON string and returns a Base64-encoded payload with
   * integrity verification. The output format is:
   * <pre>Base64(IV + ciphertext + HMAC)</pre>
   *
   * @param jsonString The plaintext JSON to encrypt.
   * @return A Base64-encoded string safe for file storage.
   * @throws SaveCryptoException if encryption fails.
   */
  public static String encrypt(String jsonString) {
    try {
      byte[] plaintext = jsonString.getBytes(StandardCharsets.UTF_8);

      // Generate random IV for each save operation
      byte[] iv = new byte[IV_LENGTH];
      new SecureRandom().nextBytes(iv);

      Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
      cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(AES_KEY, "AES"), new IvParameterSpec(iv));
      byte[] ciphertext = cipher.doFinal(plaintext);

      // Combine IV + ciphertext
      byte[] ivAndCiphertext = new byte[IV_LENGTH + ciphertext.length];
      System.arraycopy(iv, 0, ivAndCiphertext, 0, IV_LENGTH);
      System.arraycopy(ciphertext, 0, ivAndCiphertext, IV_LENGTH, ciphertext.length);

      // Compute HMAC over IV + ciphertext
      byte[] hmac = computeHmac(ivAndCiphertext);

      // Final payload: IV + ciphertext + HMAC
      byte[] payload = new byte[ivAndCiphertext.length + HMAC_LENGTH];
      System.arraycopy(ivAndCiphertext, 0, payload, 0, ivAndCiphertext.length);
      System.arraycopy(hmac, 0, payload, ivAndCiphertext.length, HMAC_LENGTH);

      return Base64.getEncoder().encodeToString(payload);
    } catch (Exception e) {
      throw new SaveCryptoException("Encryption failed", e);
    }
  }

  /**
   * Decrypts a Base64-encoded payload, verifying HMAC integrity first.
   * Rejects tampered files by throwing {@link SaveTamperedException}.
   *
   * @param base64Data The Base64 string read from the save file.
   * @return The original JSON plaintext.
   * @throws SaveTamperedException if the file has been modified.
   * @throws SaveCryptoException if decryption fails for other reasons.
   */
  public static String decrypt(String base64Data) {
    try {
      byte[] payload = Base64.getDecoder().decode(base64Data);

      if (payload.length < IV_LENGTH + HMAC_LENGTH) {
        throw new SaveTamperedException("Save file is corrupted or truncated.");
      }

      // Extract components
      int ivAndCiphertextLength = payload.length - HMAC_LENGTH;
      byte[] ivAndCiphertext = Arrays.copyOfRange(payload, 0, ivAndCiphertextLength);
      byte[] storedHmac = Arrays.copyOfRange(payload, ivAndCiphertextLength, payload.length);

      // Verify HMAC before decryption
      byte[] computedHmac = computeHmac(ivAndCiphertext);
      if (!constantTimeEquals(storedHmac, computedHmac)) {
        throw new SaveTamperedException("Save file integrity check failed. The file may have been modified.");
      }

      // Extract IV and ciphertext
      byte[] iv = Arrays.copyOfRange(ivAndCiphertext, 0, IV_LENGTH);
      byte[] ciphertext = Arrays.copyOfRange(ivAndCiphertext, IV_LENGTH, ivAndCiphertext.length);

      Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
      cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(AES_KEY, "AES"), new IvParameterSpec(iv));
      byte[] plaintext = cipher.doFinal(ciphertext);

      return new String(plaintext, StandardCharsets.UTF_8);
    } catch (SaveTamperedException e) {
      throw e;
    } catch (Exception e) {
      throw new SaveCryptoException("Decryption failed", e);
    }
  }

  /** Computes HMAC-SHA256 over the given data. */
  private static byte[] computeHmac(byte[] data) throws Exception {
    Mac mac = Mac.getInstance(HMAC_ALGORITHM);
    mac.init(new SecretKeySpec(HMAC_KEY, HMAC_ALGORITHM));
    return mac.doFinal(data);
  }

  /** Constant-time comparison to prevent timing attacks on HMAC verification. */
  private static boolean constantTimeEquals(byte[] a, byte[] b) {
    if (a.length != b.length) return false;
    int result = 0;
    for (int i = 0; i < a.length; i++) {
      result |= a[i] ^ b[i];
    }
    return result == 0;
  }

  /** Thrown when save file integrity verification fails. */
  public static class SaveTamperedException extends RuntimeException {
    public SaveTamperedException(String message) {
      super(message);
    }
  }

  /** Thrown when an unexpected crypto operation fails. */
  public static class SaveCryptoException extends RuntimeException {
    public SaveCryptoException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
