package org.zzl.minegaming.GBAUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class GBARom
{
	private String headerCode = "";
	private String headerName = "";
	private String headerMaker = "";

	static byte[] rom_bytes;
	String input_filepath;
	static byte[] current_rom_header;

	HashMap<String, String> rom_header_names = new HashMap<String, String>();
	HashMap<String, String> hex_tbl = new HashMap<String, String>();

	/**
	 *  Wraps that ROM up like a nice warm burrito
	 * @param rom_path Path to the ROM file
	 */
	public GBARom(String rom_path)
	{
		input_filepath = rom_path;

		try
		{
			loadRomToBytes();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		headerCode = readText(0xAC,4);
		headerName = readText(0xA0, 12).trim();
		headerMaker = readText(0xB0, 2);

		updateROMHeaderNames();
	}

	/**
	 *  Loads the files from the ROM into the byte array
	 * @throws IOException
	 */
	public void loadRomToBytes() throws IOException
	{
		File file = new File(input_filepath);

		InputStream is = new FileInputStream(file);
		long length = file.length();
		rom_bytes = new byte[(int) length];
		int offset = 0, n = 0;
		while (offset < rom_bytes.length
				&& (n = is.read(rom_bytes, offset, rom_bytes.length - offset)) >= 0)
		{
			offset += n;
		}
		is.close();
	}

	/**
	 *  Read bytes from the ROM from given offset into an array of a given size
	 * @param offset Offset in ROM as hex string
	 * @param size Amount of bytes to grab
	 * @return
	 */
	public byte[] readBytesFromROM(String offset, int size)
	{
		int offs = convertOffsetToInt(offset);
		return readBytesFromROM(offs, size);
	}
	
	/**
	 *  Read bytes from the ROM from given offset into an array of a given size
	 * @param offset Offset in ROM
	 * @param size Amount of bytes to grab
	 * @return
	 */
	public byte[] readBytesFromROM(int offset, int size)
	{
		return BitConverter.GrabBytes(rom_bytes, offset, size);
	}

	/**
	 *  Write an array of bytes to the ROM at a given offset
	 * @param offset Offset to write the bytes at
	 * @param bytes_to_write Bytes to write to the ROM
	 */
	public void writeBytesToROMArray(String offset, byte[] bytes_to_write)
	{
		int offs = convertOffsetToInt(offset);

		for (int count = 0; count < bytes_to_write.length; count++)
		{
			rom_bytes[offs] = bytes_to_write[count];
			offs++;
		}
	}

	/**
	 *  Write any changes made back to the ROM file on disk
	 * @return
	 */
	@SuppressWarnings("resource")
	public int commitChangesToROMFile()
	{
		FileOutputStream fos = null;

		try
		{
			fos = new FileOutputStream(input_filepath);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return 1;
		}
		try
		{
			fos.write(rom_bytes);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return 2;
		}
		try
		{
			fos.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return 3;
		}
		return 0;
	}

	/**
	 *  Convert a string offset i.e 0x943BBD into a decimal
	 *  Used for directly accessing the ROM byte array
	 * @param offset Offset to convert to an integer
	 * @return The offset as an int
	 */
	public int convertOffsetToInt(String offset)
	{
		return Integer.parseInt(offset, 16);
	}

	/**
	 *  Retrieve the header of the ROM, based on offset and size
	 *  Identical to readBytesFromROM just with a different name
	 * @param header_offset
	 * @param header_size
	 * @return
	 */
	@Deprecated
	public byte[] getROMHeader(String header_offset, int header_size)
	{
		current_rom_header = readBytesFromROM(header_offset, header_size);
		return current_rom_header;
	}

	/**
	 *  Validate the file loaded based on a given byte and offset
	 * @param validation_offset Offset to check in the ROM
	 * @param validation_byte Byte to check it with
	 * @return
	 */
	public Boolean validateROM(int validation_offset, byte validation_byte)
	{
		if (rom_bytes[validation_offset] == validation_byte)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 *  Load a HEX table file for character mapping i.e. Pokétext
	 * @param tbl_path File path to the character table
	 * @throws IOException
	 */
	public void loadHexTBL(String tbl_path) throws IOException
	{
		File file = new File(tbl_path);

		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		while ((line = br.readLine()) != null)
		{
			String[] seperated = line.split("=");
			String key;
			String value;

			if (seperated.length > 1)
			{
				key = seperated[0];
				value = seperated[1];
			}
			else
			{
				key = seperated[0];
				value = " ";
			}

			hex_tbl.put(key, value);
		}
		br.close();
	}

	/**
	 *  Convert Poketext to ascii, takes an array of bytes of poketext
	 *  Basically returns the results from the given HEX Table <- must loadHexTBL first
	 * @param poketext Poketext as a byte array
	 * @return
	 */
	public String convertPoketextToAscii(byte[] poketext)
	{
		StringBuilder converted = new StringBuilder();

		for (int i = 0; i < poketext.length; i++)
		{
			String temp;
			temp = hex_tbl.get(String.format("%02X", poketext[i]));

			converted.append(temp);
		}

		return converted.toString().trim();
	}

	/**
	 *  Return a string of the friendly ROM header based on the current ROM
	 * @return
	 */
	public String getFriendlyROMHeader()
	{
		return rom_header_names.get(new String(current_rom_header));
	}

	// Update the list of friendly ROM headers
	// TODO: Load header list from file or .ini and include inside the tool
	private void updateROMHeaderNames()
	{
		rom_header_names.put("POKEMON FIREBPRE01", "Pokémon: FireRed");
		rom_header_names.put("POKEMON LEAFBPGE01", "Pokémon: LeafGreen");
		rom_header_names.put("POKEMON EMERBPEE01", "Pokémon: Emerald");
	}

	/**
	 *  Read a structure of data from the ROM at a given offset, a set numner of times, with a set structure size
	 *  For example returning the names of Pokemon into an ArrayList of bytes
	 * @param offset Offset to read the structure from
	 * @param amount Amount to read
	 * @param max_struct_size Maximum structure size
	 * @return
	 */
	public ArrayList<byte[]> loadArrayOfStructuredData(int offset,
			int amount, int max_struct_size)
	{
		ArrayList<byte[]> data = new ArrayList<byte[]>();
		int offs = offset;

		for (int count = 0; count < amount; count++)
		{
			byte[] temp_byte = new byte[max_struct_size];

			for (int c2 = 0; c2 < temp_byte.length; c2++)
			{
				temp_byte[c2] = rom_bytes[offs];
				offs++;
			}

			data.add(temp_byte);
		}

		return data;
	}

	/**
	 * Reads ASCII text from the ROM
	 * @param offset The offset to read from
	 * @param length The amount of text to read
	 * @return Returns the text as a String object
	 */
	public String readText(int offset, int length)
	{
		return new String(BitConverter.GrabBytes(rom_bytes, offset, length));
	}
	
	public String readPokeText(int offset)
	{
		return readPokeText(offset, -1);
	}
	
	public String readPokeText(int offset, int length)
	{
		//TODO: Implement PokeText reader
		return "";
	}
	
	public byte[] getData()
	{
		return rom_bytes;
	}
	
	public String getGameCode()
	{
		return headerCode;
	}
	
	public String getGameText()
	{
		return headerName;
	}
	
	public String getGameCreatorID()
	{
		return headerMaker;
	}
}
