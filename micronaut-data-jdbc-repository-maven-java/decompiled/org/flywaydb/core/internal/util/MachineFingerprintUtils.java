package org.flywaydb.core.internal.util;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MachineFingerprintUtils {
   public static String getFingerprint(String... salts) throws Exception {
      if (salts != null && salts.length != 0 && !Arrays.stream(salts).noneMatch(StringUtils::hasText)) {
         byte[] hashedId = salts[0].getBytes(StandardCharsets.UTF_8);

         for(String salt : salts) {
            hashedId = getHashed(salt.getBytes(StandardCharsets.UTF_8), hashedId);
         }

         List<byte[]> hardwareAddresses = getHardwareAddresses();
         if (hardwareAddresses.size() == 0) {
            throw new Exception("No hardware addresses found when creating fingerprint");
         } else {
            for(byte[] hardwareAddress : hardwareAddresses) {
               hashedId = getHashed(hardwareAddress, hashedId);
            }

            return hashToString(hashedId);
         }
      } else {
         throw new Exception("All parameters required for getFingerprint");
      }
   }

   private static List<byte[]> getHardwareAddresses() throws SocketException {
      Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
      return (List<byte[]>)(networkInterfaces == null
         ? new ArrayList()
         : (List)Collections.list(networkInterfaces)
            .stream()
            .map(MachineFingerprintUtils::extractHardwareAddress)
            .filter(Objects::nonNull)
            .collect(Collectors.toList()));
   }

   private static byte[] extractHardwareAddress(NetworkInterface networkInterface) {
      try {
         return networkInterface.getHardwareAddress();
      } catch (SocketException var2) {
         return null;
      }
   }

   private static byte[] getHashed(byte[] salt, byte[] digest) throws NoSuchAlgorithmException {
      MessageDigest md = MessageDigest.getInstance("SHA-512");
      md.update(salt);
      return md.digest(digest);
   }

   private static String hashToString(byte[] hashedId) {
      String[] hexadecimal = new String[hashedId.length];

      for(int i = 0; i < hexadecimal.length; ++i) {
         hexadecimal[i] = String.format("%02X", hashedId[i]);
      }

      return String.join("", hexadecimal);
   }

   private MachineFingerprintUtils() {
   }
}
