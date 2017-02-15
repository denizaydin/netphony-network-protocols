package es.tid.bgp.bgp4.update.fields;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.tid.bgp.bgp4.update.MalformedBGP4ElementException;

/**
 * RFC 4364 BGP/MPLS IP VPNs
 * As stated, a VPN-IPv4 address consists of an 8-byte Route
   Distinguisher followed by a 4-byte IPv4 address.  The RDs are encoded
   as follows:

     - Type Field: 2 bytes
     - Value Field: 6 bytes

   The interpretation of the Value field depends on the value of the
   type field.  At the present time, three values of the type field are
   defined: 0, 1, and 2.

     - Type 0: The Value field consists of two subfields:

         * Administrator subfield: 2 bytes
         * Assigned Number subfield: 4 bytes

       The Administrator subfield must contain an Autonomous System
       number.  If this ASN is from the public ASN space, it must have
       been assigned by the appropriate authority (use of ASN values
       from the private ASN space is strongly discouraged).  The
       Assigned Number subfield contains a number from a numbering space
       that is administered by the enterprise to which the ASN has been
       assigned by an appropriate authority.

     - Type 1: The Value field consists of two subfields:

         * Administrator subfield: 4 bytes
         * Assigned Number subfield: 2 bytes

       The Administrator subfield must contain an IP address.  If this
       IP address is from the public IP address space, it must have been
       assigned by an appropriate authority (use of addresses from the
       private IP address space is strongly discouraged).  The Assigned
       Number subfield contains a number from a numbering space which is
       administered by the enterprise to which the IP address has been
       assigned.
*/

public  class VPN4_Reach_NLRI extends NLRI {
	/**
	 * Be carefull about the label while encoding! Label is not fully deployed
	 * default value is 851
	 */
	private int label;
	private String rd;
	/**
	 * RD Type Default value is Type 0, ASN:2Byte + AN:4Byte
	 */
	private int rdType;
	private String rdASN;
	private String rdAN;
	private InetAddress prefix;
	/**
	 * Prefix Length, 1-32
	 */
	private int prefixlength;
	private int totalprefixlenght;
	private byte bytes[];

	protected static final Logger log = LoggerFactory.getLogger("BGP4Parser");
	

	public VPN4_Reach_NLRI(){
		super();
		this.label=13617; // with exp etc bits.
		this.rdType=0;
		this.totalprefixlenght=3*8+8*8+this.prefixlength;
		//how many octets do we need?
		this.length=((int) Math.ceil((double)totalprefixlenght / 8)+1); // plus one more for the length octet
	}
	
	public VPN4_Reach_NLRI(byte []bytes, int offset) {
		int decimallength=((int)bytes[offset] & 0xFF);
		//log.debug("VPN4_NLRI: TotalLength int decimal "+decimallength);
		int bottomOfLabelStack = 0;
		this.prefixlength=decimallength;
		this.length =( (int) Math.ceil((double)decimallength / 8))+1; //how many octets is prefixlength(plus its octet)
		offset=offset+1;//move to the label
		while (bottomOfLabelStack==0) {
			this.prefixlength=this.prefixlength-(3*8); // remove the label bits
			if ((int) bytes[offset+2] <= 15)
			{
				bottomOfLabelStack = 1; //reserved labels
			}
			if (bottomOfLabelStack == 0) {
				bottomOfLabelStack = (bytes[offset+2]&0x01);
			}
			offset=offset+3;//move to the next label or rd
		}
		this.setRdType(((bytes[offset]&0xFF)<<8) |  (bytes[offset+1]&0xFF));
		if((this.rdType != 0 && this.rdType != 1)) {
			String msg = "Unsupported RD Type:"+this.rdType;
			throw new MalformedBGP4ElementException(msg);	
		}
		
		if (this.rdType == 0) {
			// rt asn 2 octet
			int asn=((bytes[offset+2]&0xFF)<<8) |  (bytes[offset+3]&0xFF);
			int an=((bytes[offset+4]&0xFF)<<24)|((bytes[offset+5]&0xFF)<<16)|((bytes[offset+6]&0xFF)<<8) | (bytes[offset+7]&0xFF);
			this.rdASN=Integer.toString(asn);
			this.rdAN=Integer.toString(an);
			this.setRdASN(this.rdASN);
			this.setRdAN(this.rdAN);
		} else if (this.rdType == 1) {
			String asn=(bytes[offset+2]&0xFF)+"."+(bytes[offset+3]&0xFF)+"."+(bytes[offset+4]&0xFF)+"."+(bytes[offset+5]&0xFF);
			int an=((bytes[offset+6]&0xFF)<<8) |  (bytes[offset+7]&0xFF);
			this.rdASN=asn;
			this.rdAN=Integer.toString(an);
			this.setRdASN(this.rdASN);
			this.setRdAN(this.rdAN);
		}
		offset=offset+8;
		this.prefixlength=this.prefixlength-(8*8); //remove the rd bits
	    byte[] nexthopbytes = new byte[4];
	    Arrays.fill( nexthopbytes, (byte) 0 );
		int i=0;
	    while (prefixlength-(i*8)>0)  {
			nexthopbytes[i]=bytes[i+offset];
			i++;
	    }
		
	    try {
			this.prefix=Inet4Address.getByAddress(nexthopbytes);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	



	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public String getRd() {
		return rd;
	}

	public void setRd(String rd) {
		this.rd = rd;
	}

	public InetAddress getPrefix() {
		return prefix;
	}

	public void setPrefix(InetAddress prefix) {
		this.prefix = prefix;
	}

	public void encode() {
		this.bytes=new byte[this.getLength()];
		//encode prefix length
		int offset=0;
		this.bytes[offset]=(byte) this.totalprefixlenght;
		offset=offset+1;
		//FIXME: Only one label is supported
		//encode label
			for(int d = 0; d < 2; d++)
			{
				this.bytes[offset] = (byte) ((this.label >> 16) & 0xFF);
				this.bytes[offset+1] = (byte) ((this.label >> 8) & 0xFF);
				this.bytes[offset+2] = (byte) (this.label & 0xFF);
			}
		offset=offset+3;
		//encode rd
		if (this.rdType == 0) {
			this.bytes[offset]=0;
			this.bytes[offset+1]=0;
			// rd asn 2 octet
			int asn=Integer.parseInt(this.rdASN);
	    	this.bytes[offset+2]=(byte)((asn>>8)&0xFF);
	    	this.bytes[offset+3]=(byte)(asn&0xFF);
	    	// rd an 4 octet
	    	int an=Integer.parseInt(this.rdAN);
	    	this.bytes[offset+4] = (byte) ((an >> 24) & 0xFF);
	    	this.bytes[offset+5] =  (byte) ((an >> 16) & 0xFF);
	    	this.bytes[offset+6] =  (byte) ((an >> 8) & 0xFF);
	    	this.bytes[offset+7] = (byte) (an & 0xFF);
		} else if (this.rdType == 1) {
			this.bytes[offset]=0;
			this.bytes[offset+1]=1;
			// rd asn 4 octet must be ip address
	        InetAddress asnASip;
			try {
				asnASip=Inet4Address.getByName(this.rdASN);
		    	System.arraycopy(asnASip.getAddress(), 0, bytes, offset+2, 4);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new MalformedBGP4ElementException("VPN4 NLRI RD ASN value is not appropriate");

			}
			// rd an 2 octet
			int an=Integer.parseInt(this.getRdAN());
	    	this.bytes[offset+6]=(byte)((an>>8)&0xFF);
	    	this.bytes[offset+7]=(byte)(an&0xFF);
			
		} else {
			// TODO Auto-generated catch block
			String msg = "Unsupported NLRI RD Type:"+this.getRdType();
			throw new MalformedBGP4ElementException(msg);	
		}
		offset=offset+8;

		// without zeros at the end of the prefix 192.168.2.0/24 must be repsented as 192.168.2/24
		int prefixLengtInOctets=(int) Math.ceil((double)(this.prefixlength) / 8);
    	System.arraycopy(this.prefix.getAddress(), 0, this.bytes, offset, prefixLengtInOctets);
    	offset=offset+prefixLengtInOctets;
	}
	



	@Override
	public byte[] getBytes()
	{
		if(this.bytes == null) encode();
		return this.bytes;
	}

	public int getLabel() {
		return label;
	}

	public void setLabel(int label) {
		this.label = label;
	}

	public int getRdType() {
		return rdType;
	}

	public void setRdType(int rdType) {
		this.rdType = rdType;
	}

	public String getRdASN() {
		return rdASN;
	}

	public void setRdASN(String rdASN) {
		this.rdASN = rdASN;
	}

	public String getRdAN() {
		return rdAN;
	}

	public void setRdAN(String rdAN) {
		this.rdAN = rdAN;
	}

	public int getPrefixlength() {
		return prefixlength;
	}

	public void setPrefixlength(int prefixlength) {
		this.prefixlength = prefixlength;
		this.totalprefixlenght=3*8+8*8+this.prefixlength;
		//how many octets do we need?
		this.length=((int) Math.ceil((double)totalprefixlenght / 8)+1); // plus one more octet for length octet
	}

	public int getTotalprefixlenght() {
		return totalprefixlenght;
	}

	public void setTotalprefixlenght(int totalprefixlenght) {
		this.totalprefixlenght = totalprefixlenght;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		VPN4_Reach_NLRI other = (VPN4_Reach_NLRI) obj;

		if (length != other.length)
			return false;
		return true;
	}
	
	public String toString() {
		//String sb = super.toString();
		String ret="Prefix:RD"+":"+this.getRdASN()+":"+this.getRdAN()+":"+this.getPrefix().getHostAddress()+"/"+ this.getPrefixlength();
		return ret;
	}
	
	
	

}
