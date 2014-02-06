package org.zzl.minegaming.GBAUtils;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class GBARom implements Cloneable
{
	private String headerCode = "";
	private String headerName = "";
	private String headerMaker = "";

	static byte[] rom_bytes;
	public String input_filepath;
	static byte[] current_rom_header;

	HashMap<String, String> rom_header_names = new HashMap<String, String>();
	HashMap<String, String> hex_tbl = new HashMap<String, String>();

	@SuppressWarnings("deprecation")
	/**
	 * Loads a ROM using a file dialog. Sets the loaded ROM as default.
	 * @return The ROMManager ROM Id.
	 */
	public static int loadRom()
	{
		FileDialog fd = new FileDialog(new Frame(), "Load a ROM...", FileDialog.LOAD);
		fd.setFilenameFilter(new FilenameFilter()
		{
		    public boolean accept(File dir, String name)
		    {
		      return (name.toLowerCase().endsWith(".gba") || name.toLowerCase().endsWith(".bin") || name.toLowerCase().endsWith(".rbc") || name.toLowerCase().endsWith(".rbh") || name.toLowerCase().endsWith(".but") || name.toLowerCase().endsWith(".bmp"));
		    }
		 });
		//fd.setDirectory(GlobalVars.LastDir);
		fd.show();
		String location = fd.getDirectory() + fd.getFile();
		if(location.isEmpty())
			return -1;
		int romID = ROMManager.getID();
		ROMManager.AddROM(romID, new GBARom(location));
		ROMManager.ChangeROM(romID);
		
		if(ROMManager.getActiveROM().hex_tbl.isEmpty())
		{
			try
			{
				//String path = LZ77Test.class.getProtectionDomain().getCodeSource().getLocation().getPath();
				//String decodedPath = URLDecoder.decode(path, "UTF-8");
				ROMManager.getActiveROM().loadHexTBL("/resources/poketable.tbl");
			}
			catch (IOException e)
			{
				e.printStackTrace();
				return -1;
			}
		}
		
		return romID;
	}
	
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
	public byte[] readBytes(String offset, int size)
	{
		int offs = convertOffsetToInt(offset);
		return readBytes(offs, size);
	}

	/**
	 *  Read bytes from the ROM from given offset into an array of a given size
	 * @param offset Offset in ROM
	 * @param size Amount of bytes to grab
	 * @return
	 */
	public byte[] readBytes(int offset, int size)
	{
		return BitConverter.GrabBytes(rom_bytes, offset, size);
	}
	public byte[] readBytes( int size)
	{
		byte[] t=BitConverter.GrabBytes(rom_bytes, internalOffset, size);
		internalOffset+=size;	
		return t;
	}
	/**
	 * Reads a byte from an offset
	 * @param offset Offset to read from
	 * @return
	 */
	public byte readByte(int offset)
	{
		return readBytes(offset,1)[0];
	}
	public byte readByte()
	{
		byte t = rom_bytes[internalOffset];
		internalOffset+=1;
		return t;
	}
	/**
	 * Reads a byte from an offset
	 * @param offset Offset to read from
	 * @return
	 */
	public int readByteAsInt(int offset)
	{
		return BitConverter.ToInts(readBytes(offset,1))[0];
	}
	/**
	 * Reads a byte from an internal offset
	 * @param offset Offset to read from
	 * @return
	 */
	public int readByteAsInt()
	{
		int tmp=BitConverter.ToInts(readBytes(internalOffset,1))[0];
		internalOffset++;
		return tmp;
	}
	public long readLong()
	{
		byte[] t=readBytes(4);
		internalOffset+=4;
		return BitConverter.ToInt32(t);
		
	}
	public long readLong(int offset)
	{
		byte[] t=readBytes(offset, 4);
		return BitConverter.ToInt32(t);
		
	}
	/**
	 * Reads a 16 bit word from an offset
	 * @param offset Offset to read from
	 * @return
	 */
	public int readWord(int offset)
	{
		int[] words = BitConverter.ToInts(readBytes(offset,2));
		return (words[1] << 8) + (words[0]);
	}
	
	public void writeWord(int offset, int toWrite)
	{
		int[] bytes = new int[] {toWrite & 0xFF, (toWrite & 0xFF00) >> 8};
		byte[] nBytes = BitConverter.toBytes(bytes);
		writeBytes(offset,nBytes);
	}
	
	public void writeWord(int toWrite)
	{
		writeWord(internalOffset,toWrite);
		internalOffset += 2;
	}
	
	/**
	 * Reads a 16 bit word from an InternalOffset
	 * @param offset Offset to read from
	 * @return
	 */
	public int readWord()
	{
		int[] words = BitConverter.ToInts(readBytes(internalOffset,2));
		internalOffset+=2;
		return (words[1] << 8) + (words[0]);
	}
	/**
	 *  Write an array of bytes to the ROM at a given offset
	 * @param offset Offset to write the bytes at
	 * @param bytes_to_write Bytes to write to the ROM
	 */
	public void writeBytes(int offset, byte[] bytes_to_write)
	{
		for (int count = 0; count < bytes_to_write.length; count++)
		{
			rom_bytes[offset] = bytes_to_write[count];
			offset++;
		}
	}
	
	public void writeByte(byte b, int offset)
	{
		rom_bytes[offset] = b;
	}
	
	public void writeByte(byte b)
	{
		rom_bytes[internalOffset] = b;
		internalOffset++;
	}
	
    public int internalOffset;
    /**
	 *  Write an array of bytes to the ROM at a given offset
	 * @param offset Offset to write the bytes at
	 * @param bytes_to_write Bytes to write to the ROM
	 */
    public void writeBytes(byte[] bytes_to_write)
	{
		for (int count = 0; count < bytes_to_write.length; count++)
		{
			rom_bytes[internalOffset] = bytes_to_write[count];
			internalOffset++;
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
		current_rom_header = readBytes(header_offset, header_size);
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
	public void loadHexTBLFromFile(String tbl_path) throws IOException
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
	 *  Load a HEX table file for character mapping i.e. Pokétext
	 * @param tbl_path File path to the character table
	 * @throws IOException
	 */
	public void loadHexTBL(String tbl_path) throws IOException
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(GBARom.class.getResourceAsStream(tbl_path)));
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
		int offs = offset & 0x1FFFFFF;
      
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
		if(length > -1)
			return convertPoketextToAscii(BitConverter.GrabBytes(getData(), offset, length));
		
		byte b = 0x0;
		int i = 0;
		while(b != -1)
		{
			b = getData()[offset+i];
			i++;
		}
		
		return convertPoketextToAscii(BitConverter.GrabBytes(getData(), offset, i));
	}
	
	public String readPokeText()
	{
		byte b = 0x0;
		int i = 0;
		while(b != -1)
		{
			b = getData()[internalOffset+i];
			i++;
		}
		
		String s = convertPoketextToAscii(BitConverter.GrabBytes(getData(), internalOffset, i));
		internalOffset += i;
		return s;
	}
	
	public byte[] getData()
	{
		return rom_bytes;
	}
	
	/**
	 * Gets a pointer at an offset
	 * @param offset Offset to get the pointer from
	 * @param fullPointer Whether we should fetch the full 32 bit pointer or the 24 bit byte[] friendly version.
	 * @return Pointer as a Long
	 */
	public long getPointer(int offset, boolean fullPointer)
	{
		byte[] data = BitConverter.GrabBytes(getData(), offset, 4);
		data[3]=0;
		return BitConverter.ToInt32(data);
	}
	
	/**
	 * Gets a 24 bit pointer in the ROM as an integer. 
	 * @param offset Offset to get the pointer from
	 * @return Pointer as a Long
	 */
	public long getPointer(int offset)
	{
		return getPointer(offset,false)& 0x1FFFFFF;
	}
	
	/**
	 * Gets a pointer in the ROM as an integer. 
	 * Does not support 32 bit pointers due to Java's integer size not being long enough.
	 * @param offset Offset to get the pointer from
	 * @return Pointer as an Integer
	 */
	public int getPointerAsInt(int offset)
	{
		return (int)getPointer(offset,false);
	}
	
	public long getSignedLong(boolean fullPointer)
	{
		byte[] data = BitConverter.GrabBytes(getData(), internalOffset, 4);
		if(!fullPointer)
			data[3] = 0;
		internalOffset+=4;
		long ptr = BitConverter.ToInt32(data);
		return (data[3] > 0x7F ? ~ptr : ptr);
	}
	
	/**
	 * Reverses and writes a pointer to the ROM
	 * @param pointer Pointer to write
	 * @param offset Offset to write it at
	 */
	public void writePointer(long pointer, int offset)
	{
		byte[] bytes = BitConverter.GetBytes(pointer);
		writeBytes(offset,bytes);
	}
	
	/**
	 * Reverses and writes a pointer to the ROM. Assumes pointer is ROM memory and appends 08 to it.
	 * @param pointer Pointer to write (appends 08 automatically)
	 * @param offset Offset to write it at
	 */
	public void writePointer(int pointer, int offset)
	{
		byte[] bytes = BitConverter.GetBytes(pointer);
		bytes[3] = 0x08;
		writeBytes(offset,bytes);
	}
	
	/**
	 * Gets the game code from the ROM, ie BPRE for US Pkmn Fire Red
	 * @return
	 */
	public String getGameCode()
	{
		return headerCode;
	}
	
	/**
	 * Gets the game text from the ROM, ie POKEMON FIRE for US Pkmn Fire Red
	 * @return
	 */
	public String getGameText()
	{
		return headerName;
	}
	
	/**
	 * Gets the game creator ID as a String, ie '01' is GameFreak's Company ID
	 * @return
	 */
	public String getGameCreatorID()
	{
		return headerMaker;
	}

	/**
	 * Gets a pointer at an offset
	 * @param offset Offset to get the pointer from
	 * @param fullPointer Whether we should fetch the full 32 bit pointer or the 24 bit byte[] friendly version.
	 * @return Pointer as a Long
	 */
	public long getPointer(boolean fullPointer)
	{
		byte[] data = BitConverter.GrabBytes(getData(), internalOffset, 4);
		if(!fullPointer)
			data[3] -= 0x8;
		internalOffset+=4;
		return BitConverter.ToInt32(data);
	}
	
	/**
	 * Gets a 24 bit pointer in the ROM as an integer. 
	 * @param offset Offset to get the pointer from
	 * @return Pointer as a Long
	 */
	public long getPointer()
	{
		return getPointer(false);
	}
	
	/**
	 * Gets a pointer in the ROM as an integer. 
	 * Does not support 32 bit pointers due to Java's integer size not being long enough.
	 * @param offset Offset to get the pointer from
	 * @return Pointer as an Integer
	 */
	public int getPointerAsInt()
	{
		return (int)getPointer(internalOffset,false);
	}
	
	/**
	 * Reverses and writes a pointer to the ROM
	 * @param pointer Pointer to write
	 * @param offset Offset to write it at
	 */
	public void writePointer(long pointer)
	{
		byte[] bytes = BitConverter.ReverseBytes(BitConverter.GetBytes(pointer));

		writeBytes(internalOffset,bytes);
		internalOffset+=4;
	}
	
	public void writeSignedPointer(long pointer)
	{
		byte[] bytes = BitConverter.ReverseBytes(BitConverter.GetBytes(pointer));

		writeBytes(internalOffset,bytes);
		internalOffset+=4;
	}
	
	/**
	 * Reverses and writes a pointer to the ROM. Assumes pointer is ROM memory and appends 08 to it.
	 * @param pointer Pointer to write (appends 08 automatically)
	 * @param offset Offset to write it at
	 */
	public void writePointer(int pointer)
	{
		byte[] bytes = BitConverter.ReverseBytes(BitConverter.GetBytes(pointer));
		bytes[3] += 0x8;

		writeBytes(internalOffset,bytes);
		internalOffset+=4;
	}
	
	/**
	 * Gets the game code from the ROM, ie BPRE for US Pkmn Fire Red
	 * @return
	 */
	public void Seek(int offset)
	{
		if(offset > 0x08000000)
			offset &= 0x1FFFFFF;
		
		internalOffset=offset;
	}

	public byte freeSpaceByte = (byte)0xFF;
	public int findFreespace(int length)
	{
		return findFreespace(length, 0);
	}
	
	public int findFreespace(long freespaceStart, int startingLocation)
	{
		byte free = freeSpaceByte;
		 byte[] searching = new byte[(int) freespaceStart];
		 for(int i = 0; i < freespaceStart; i++)
			 searching[i] = free;
		 int numMatches = 0;
		 int freespace = -1;
		 for(int i = startingLocation; i < rom_bytes.length; i++)
		 {
			 byte b = rom_bytes[i];
			 byte c = searching[numMatches];
			 if(b == c)
			 {
				 numMatches++;
				 if(numMatches == searching.length - 1)
				 {
					 freespace = i - searching.length + 2;
					 break;
				 }
			 }
			 else
				 numMatches = 0;
		 }
		 return freespace;
	}
	
	public void floodBytes(int offset, byte b, int length)
	{
		for(int i = offset; i < offset+length; i++)
			rom_bytes[i] = b;
	}
	
	public void repoint(int pOriginal, int pNew)
	{
		repoint(pOriginal, pNew, -1);
	}
	
	public void repoint(int pOriginal, int pNew, int numbertolookfor)
	{
		pOriginal |= 0x08000000;
		 byte[] searching = BitConverter.ReverseBytes(BitConverter.GetBytes(pOriginal));
		 int numMatches = 0;
		 int totalMatches = 0;
		 int offset = -1;
		 for(int i = 0; i < rom_bytes.length; i++)
		 {
			 byte b = rom_bytes[i];
			 byte c = searching[numMatches];
			 if(b == c)
			 {
				 numMatches++;
				 if(numMatches == searching.length - 1)
				 {
					 offset = i - searching.length + 2;
					 this.Seek(offset);
					 this.writePointer(pNew);
					 System.out.println(BitConverter.toHexString(offset));
					 totalMatches++;
					 if(totalMatches == numbertolookfor)
						 break;
					 numMatches = 0;
				 }
			 }
			 else
				 numMatches = 0;
		 }
		 System.out.println("Found " + totalMatches + " occurences of the pointer specified.");
	}
	
	public Object clone(){  
	    try{  
	        return super.clone();  
	    }catch(Exception e){ 
	        return null; 
	    }
	}
}
