package es.tid.bgp.bgp4.update.fields.pathAttributes;

import es.tid.bgp.bgp4.BGP4Element;
import es.tid.bgp.bgp4.update.MalformedBGP4ElementException;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * RFC 4360 BGP Extended Communities Attribute
 * http://www.iana.org/assignments/bgp-extended-communities/bgp-extended-communities.xhtml
 * <TYPE><SUBTYPE>:<GLOBAL_ADMINISTRATOR>:<LOCAL_ADMINISTRATOR>
 * Class is based on 3 important values of Extended Communitie Type
 * The first three values divide the 6 remaining octets in the Value Field into a defined Global Administrator (GA) Field and Local Administrator (LA) Field. The difference lies in the number of octets allotted to each field and the nature of the value contained in that field. The fourth Type Field value describes a variable structure.
		The values are:
		
		1. 0x00 (RFC 4360) – 2-octet AS Specific Extended Community
		
		Global Administrator – 2 octets representing the 2-byte AS number.
		Local Administrator – 4 octets populated with a unique value that represents the community
		
		2. 0x01 (RFC 4360) – IPv4 Address Specific Extended Community
		
		Global Administrator – 4 octets representing an IPv4 Address.
		Local Administrator – 2 octets populated with a unique value that represents the community
		
		3. 0x02 (RFC 5668) – 4-octet AS Specific BGP Extended Community
		
		Global Administrator – 4 octets but this time representing the 4-byte AS number.
		Local Administrator – 2 octets populated with a unique value that represents the community
* Global Administrator and Local Administrator part is represented as single variable as string
*/
public  class Extended_Communitiy_Segment  implements BGP4Element {
	/**
	 * Only Type 0 and 1 are supported. Default is Type 0
	 */
	private int type;
	/**
	 * Only Type 2, Route Target, is supported.
	 */
	private int subtype;
	private String rtASN;
	private String rtAN;
	private byte bytes[];
	private int  length;
	


	public Extended_Communitiy_Segment()
	{
		
		super();
		this.type=0;
		this.subtype=2;
		this.length=8;
	}
	
	public Extended_Communitiy_Segment(byte[] bytes, int offset) throws MalformedBGP4ElementException
	{
		this.type = (int) bytes[offset] & 0xFF;
		this.subtype = (int) bytes[offset+1] & 0xFF;

		if((this.type != 0 && this.type != 1) && (this.subtype !=2)) {
			String msg = "Unsupported Extended Community Type:"+this.type+" subtype"+this.subtype;
			throw new MalformedBGP4ElementException(msg);	
		}
		if (this.type == 0) {
			// rt asn 2 octet
			int asn=((bytes[offset+2]&0xFF)<<8) |  (bytes[offset+3]&0xFF);
			int an=((bytes[offset+4]&0xFF)<<24)|((bytes[offset+5]&0xFF)<<16)|((bytes[offset+6]&0xFF)<<8) | (bytes[offset+7]&0xFF);
			this.rtASN=Integer.toString(asn);
			this.rtAN=Integer.toString(an);
			this.setRtASN(this.rtASN);
			this.setRtAN(this.rtAN);
		} else if (this.type == 1) {
			String asn=(bytes[offset+2]&0xFF)+"."+(bytes[offset+3]&0xFF)+"."+(bytes[offset+4]&0xFF)+"."+(bytes[offset+5]&0xFF);
			int an=((bytes[offset+6]&0xFF)<<8) |  (bytes[offset+7]&0xFF);
			this.rtASN=asn;
			this.rtAN=Integer.toString(an);
			this.setRtASN(this.rtASN);
			this.setRtAN(this.rtAN);
		}
		offset=offset+8;
	}
	
	public void encode() {
		int offset = 0;
		bytes = new byte[this.length];
		this.bytes[offset] = (byte) this.type;
		this.bytes[offset+1] = (byte) this.subtype;
		offset=offset+2;
		if (this.type == 0 && this.subtype == 2) {
			// rt asn 2 octet
			int asn=Integer.parseInt(this.rtASN);
	    	this.bytes[offset]=(byte)((asn>>8)&0xFF);
	    	this.bytes[offset+1]=(byte)(asn&0xFF);
	    	// rt an 4 octet
	    	int an=Integer.parseInt(this.rtAN);
	    	this.bytes[offset+2] = (byte) ((an >> 24) & 0xFF);
	    	this.bytes[offset+3] =  (byte) ((an >> 16) & 0xFF);
	    	this.bytes[offset+4] =  (byte) ((an >> 8) & 0xFF);
	    	this.bytes[offset+5] = (byte) (an & 0xFF);
		} else if (this.type == 1 && this.subtype == 2) {
	        InetAddress asnASip;
			try {
				asnASip=Inet4Address.getByName(this.rtASN);
		    	System.arraycopy(asnASip.getAddress(), 0, bytes, offset, 4);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				throw new MalformedBGP4ElementException("Exteded Community RT ASN value is not appropriate");

			}
			// rt an 2 octet
			int an=Integer.parseInt(this.getRtAN());
	    	this.bytes[offset+6]=(byte)((an>>8)&0xFF);
	    	this.bytes[offset+7]=(byte)(an&0xFF);
			
		} else {
			String msg = "Unsupported Extended Community Type:"+this.type+" subtype"+this.subtype;
			throw new MalformedBGP4ElementException(msg);	
		}

	}
	public String getRtASN() {
		return rtASN;
	}

	public void setRtASN(String rtASN) {
		this.rtASN = rtASN;
	}

	public String getRtAN() {
		return rtAN;
	}

	public void setRtAN(String rtAN) {
		this.rtAN = rtAN;
	}


	public String toString() {
		//String sb = super.toString();
		String ret="RT:"+this.getRtASN()+":"+this.getRtAN();
		return ret;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		if (type == 0 || type == 1 ) {
		this.type = type;
		} else {
		this.type=0;
		}
	}

	public int getSubtype() {
		return subtype;
	}

	public void setSubtype(int subtype) {
		this.subtype = subtype;
	}
	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}
}
