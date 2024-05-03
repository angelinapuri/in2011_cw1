// IN2011 Computer Networks
// Coursework 2023/2024
//
// Construct the hashID for a string

import java.lang.StringBuilder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class HashID {
		public static String computeHashID(String line) throws Exception {
			if (line.endsWith("\n")) {
				// What this does and how it works is covered in a later lecture
				MessageDigest md = MessageDigest.getInstance("SHA-256");
				byte[] lineBytes = line.getBytes(StandardCharsets.UTF_8);
				md.update(lineBytes);
				byte[] hashBytes = md.digest();
				return bytesToHex(hashBytes);
			} else {
				// 2D#4 computes hashIDs of lines, i.e. strings ending with '\n'
				throw new Exception("No new line at the end of input to HashID");
			}
		}

		private static String bytesToHex(byte[] hash) {
			StringBuilder hexString = new StringBuilder(2 * hash.length);
			for (byte b : hash) {
				String hex = Integer.toHexString(0xff & b);
				if (hex.length() == 1) {
					hexString.append('0');
				}
				hexString.append(hex);
			}
			return hexString.toString();
		}

		public static int computeDistance(String H1, String H2) {
			int distance = 256; // Initialize distance to 256

			// Convert the hexadecimal strings to binary strings
			String bin1 = hexToBin(H1);
			String bin2 = hexToBin(H2);

			// Count the number of matching leading bits
			for (int i = 0; i < bin1.length(); i++) {
				if (bin1.charAt(i) == bin2.charAt(i)) {
					distance--; // Decrement distance for each matching bit
				} else {
					break; // Stop counting if bits don't match
				}
			}

			return distance;
		}

		private static String hexToBin(String hexString) {
			StringBuilder binString = new StringBuilder();
			for (char c : hexString.toCharArray()) {

				String bin = Integer.toBinaryString(Integer.parseInt(String.valueOf(c), 16));
				bin = "0000".substring(bin.length()) + bin;
				binString.append(bin);
			}
			return binString.toString();
		}

		public static void main(String[] args) throws Exception {

			System.out.println(computeHashID("Hello World!" + "\n"));

			String sirko = "dc98db702fef1fe85881eb9caddf587fb4aca668d0d5c01efd3bac1c2ead2921";
			String email = "710c3906ca8b54f86c9e020d989792d03b9a0d8904d7b57b3f57e74ab1746625";
			String test = "7bdfdc0ad1f3759a6d411ab7d27bfaadb57147d1c4f14dbb5a0e833df05fcae4";

			try {
				System.out.println("Hash 1: " + email);
				System.out.println("Hash 2: " + sirko);
				System.out.println("Distance: " + computeDistance(email, sirko));
			} catch (Exception e) {
				System.err.println("An error occurred: " + e.getMessage());
			}
		}

	}



