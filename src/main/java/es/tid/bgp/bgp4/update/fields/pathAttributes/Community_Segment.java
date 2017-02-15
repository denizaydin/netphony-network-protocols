package es.tid.bgp.bgp4.update.fields.pathAttributes;

import es.tid.bgp.bgp4.BGP4Element;
import es.tid.bgp.bgp4.update.MalformedBGP4ElementException;




/**
 * RFC 1997 : https://tools.ietf.org/html/rfc1997
 * Communities are treated as 32 bit values,  however for administrative
   assignment,  the following presumptions may be made:

   The community attribute values ranging from 0x0000000 through
   0x0000FFFF and 0xFFFF0000 through 0xFFFFFFFF are hereby reserved.

   The rest of the community attribute values shall be encoded using an
   autonomous system number in the first two octets.  The semantics of
   the final two octets may be defined by the autonomous system (e.g. AS
   690 may define research, educational and commercial community values
   that may be used for policy routing as defined by the operators of
   that AS using community attribute values 0x02B20000 through
   0x02B2FFFF).

Well-known Communities

   The following communities have global significance and their
   operations shall be implemented in any community-attribute-aware BGP
   speaker.

      NO_EXPORT (0xFFFFFF01)
         All routes received carrying a communities attribute
         containing this value MUST NOT be advertised outside a BGP
         confederation boundary (a stand-alone autonomous system that
         is not part of a confederation should be considered a
         confederation itself).
      NO_ADVERTISE (0xFFFFFF02)
         All routes received carrying a communities attribute
         containing this value MUST NOT be advertised to other BGP
         peers.
      NO_EXPORT_SUBCONFED (0xFFFFFF03)
         All routes received carrying a communities attribute
         containing this value MUST NOT be advertised to external BGP
         peers (this includes peers in other members autonomous
         systems inside a BGP confederation).
         
 * 
 * 
 * Community in format of part1:part2 (Cisco new format) 
 * http://www.cisco.com/c/en/us/about/press/internet-protocol-journal/back-issues/table-contents-24/bgp-communities.html
 * part1 two octet, typically AS Number
 * part2 two octet, any value
*/
public  class Community_Segment  implements BGP4Element {

	private int part1;
	private int part2;


	private byte bytes[];
	private int  length;


	public Community_Segment()
	{
		super();
		this.setLength(4);
	}
	
	public Community_Segment(byte[] bytes, int offset) throws MalformedBGP4ElementException
	{

		int part1=((bytes[offset]&0xFF)<<8) |  (bytes[offset+1]&0xFF);
		int part2=((bytes[offset+2]&0xFF)<<8) |  (bytes[offset+3]&0xFF);
		this.setPart1(part1);
		this.setPart2(part2);
		offset=offset+4;
	}
	
	public void encode() {
		int offset = 0;
		bytes = new byte[this.getLength()];

	    	this.bytes[offset]=(byte)((this.getPart1()>>8)&0xFF);
	    	this.bytes[offset+1]=(byte)(this.getPart1()&0xFF);
	    	
	    	this.bytes[offset+2]=(byte)((this.getPart2()>>8)&0xFF);
	    	this.bytes[offset+3]=(byte)(this.getPart2()&0xFF);
	    	offset=offset+this.getLength();

	}
	
	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}
	public int getPart1() {
		return part1;
	}

	public void setPart1(int part1) {
		this.part1 = part1;
	}

	public int getPart2() {
		return part2;
	}

	public void setPart2(int part2) {
		this.part2 = part2;
	}



	public String toString() {
		//String sb = super.toString();
		String ret=this.getPart1()+":"+this.getPart2();
		return ret;
	}



	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}
}