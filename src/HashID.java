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

/**		public static void main(String[] args) throws Exception {

		//	System.out.println(computeHashID("Hello World!" + "\n"));

			String H1 = "0f033be6cea034bd45a0352775a219ef5dc7825ce55d1f7dae9762d80ce64411";
			String H2 = "0f0139b167bb7b4a416b8f6a7e0daa7e24a08172b9892171e5fdc615bb7f999b";

			try {
				System.out.println("Hash 1: " + H1);
				System.out.println("Hash 2: " + H2);
				System.out.println("Distance: " + computeDistance(H1, H2));
			} catch (Exception e) {
				System.err.println("An error occurred: " + e.getMessage());
			}
		}
 */
	}



