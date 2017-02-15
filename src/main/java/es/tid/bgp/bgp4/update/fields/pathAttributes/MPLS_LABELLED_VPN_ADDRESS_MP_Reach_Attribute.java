package es.tid.bgp.bgp4.update.fields.pathAttributes;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import es.tid.bgp.bgp4.update.fields.VPN4_Reach_NLRI;




public class MPLS_LABELLED_VPN_ADDRESS_MP_Reach_Attribute extends MP_Reach_Attribute {
	
	private int totalNlriLength;
	
	private int nextHopLength;
 
	/** Nexthop ipv4 or ipv6 Default is ipv4 and loopback 127.0.0.1
	 * RD of the nexthop is fixed to 0:0 !
	 */
	private InetAddress nextHop;

    
    private VPN4_Reach_NLRI vpn4nlri;

    private List<VPN4_Reach_NLRI> vpn4Nlries;
    
	public MPLS_LABELLED_VPN_ADDRESS_MP_Reach_Attribute(){
		super();
		this.setAddressFamilyIdentifier(1);
		this.setSubsequentAddressFamilyIdentifier(128);
	 	try {
    		this.setNextHop(Inet4Address.getByName("127.0.0.1"));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public MPLS_LABELLED_VPN_ADDRESS_MP_Reach_Attribute(byte [] bytes, int offset) {
		super(bytes, offset);
	  	vpn4Nlries = new LinkedList<VPN4_Reach_NLRI>();
		if (this.extendedLengthBit == true) {
		this.totalNlriLength=((bytes[offset+2]&0xFF)<<8)|(bytes[offset+3]&0xFF);
		offset=offset+4;
		} else {
		this.totalNlriLength=(bytes[offset+2]&0xFF);
		offset=offset+3;
		}
		//move to the nexthop
		offset=offset+3;
		this.totalNlriLength=this.totalNlriLength-2-1-((int)(bytes[offset]&0xFF))-1-1; //remove afi, safi,nh(count the nexhoplengthbit and sap byte count
		this.nextHopLength=((int)(bytes[offset]&0xFF))-8; //remove rd length from totalnexthoplength(with rd) to get actual nexthoplength
		offset++; // move to the rd
		offset=offset+8; //skip the nexthop rd
		byte[] nexthopbytes =  new byte[this.nextHopLength];
		System.arraycopy(bytes, offset, nexthopbytes, 0, this.nextHopLength);
		if (this.nextHopLength==4){
				try {
					this.nextHop=Inet4Address.getByAddress(nexthopbytes);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		offset=offset+this.nextHopLength; //move to the sap
		offset++;//move to the nlri
		int remainingNlriLength = this.totalNlriLength;
		while (remainingNlriLength>0) {
		VPN4_Reach_NLRI tempvpn4nlri = new VPN4_Reach_NLRI(bytes, offset);
		remainingNlriLength=remainingNlriLength-tempvpn4nlri.getLength();
		vpn4Nlries.add(tempvpn4nlri);
		offset=offset+tempvpn4nlri.getLength();
		}
	  	this.setVpn4Nlries(vpn4Nlries);
	}
	
	public void encode() {
		/**
		 * Flags are setted in the MP_Reach_Atrribute Class
		 * Attribute length:
		 * Flag (1 octet)
		 * Type Code (1 octet)
		 * Lenght (1 or 2 octets) can be extended accourding the number of NLRI
		 * 	Address Family Identifier (2 octets)
		 * 	Subsequent Address Family Identifier (1 octet)
		 * 	Network Address of Next Hop (variable), assuming IPv4, 12 octets RD 8 octets 4 octet IPv4 NH //FIXME: change this in case of IPv6
		 * 	SNPA (1) octets
		 * 	Total: minumum
		 * Maximum Length of NLRI is NLRI Length, Label, RD, prefix (max 4 octet in case of IPv4) 1+3+8+4=12
		 */

	  	this.pathAttributeLength=(2+1+1+8+this.getNextHopLength()+1); //afi+safi+nexthoplenght octet+nexthoprd+nexthop+snpa
		for(int i=0; i< vpn4Nlries.size() ;++i)
		{
			vpn4Nlries.get(i).encode();
			this.pathAttributeLength=this.getPathAttributeLength()+vpn4Nlries.get(i).getLength();

		}
		this.setPathAttributeLength(this.pathAttributeLength);
		// if total lenght is more than 255 setPathAttributeLength sets it as extended length automaticaly
		this.bytes=new byte[this.getLength()];
		encodeHeader();
	   	int offset = this.getMandatoryLength();
	  	//AFI
    	this.bytes[offset]=(byte)((this.getAddressFamilyIdentifier()>>8)&0xFF);
    	this.bytes[offset+1]=(byte)(this.getAddressFamilyIdentifier() &0xFF);
    	//SAFI
    	this.bytes[offset+2]=(byte)(this.getSubsequentAddressFamilyIdentifier() &0xFF);
    	
    	this.bytes[offset+3]=(byte)(8+this.getNextHopLength()); //nexhthop lenght (1 octet) + rd (8 octet) + nexthop 
    	offset=offset+3;
    	//FIXME RD of the nexthop, Static!
    	this.bytes[offset+1]=0;
    	this.bytes[offset+2]=0;
    	this.bytes[offset+3]=0;
    	this.bytes[offset+4]=0;
    	this.bytes[offset+5]=0;
    	this.bytes[offset+6]=0;
    	this.bytes[offset+7]=0;
    	this.bytes[offset+8]=0;
    	offset=offset+9;
    	System.arraycopy(this.getNextHop().getAddress(), 0, this.bytes, offset, this.getNextHopLength());
    	offset=offset+this.getNextHopLength();
    	this.bytes[offset]=0; //SNPA octet 
    	offset=offset+1;
    	//NLRI
		for(int i=0; i< vpn4Nlries.size() ;++i)
		{
			System.arraycopy(vpn4Nlries.get(i).getBytes(), 0, this.bytes, offset, vpn4Nlries.get(i).getLength());
			offset=offset+vpn4Nlries.get(i).getLength();
		}

	}

	public VPN4_Reach_NLRI getVpn4nlri() {
		return vpn4nlri;
	}

	public void setVpn4nlri(VPN4_Reach_NLRI vpn4nlri) {
		this.vpn4nlri = vpn4nlri;
	}

	public List<VPN4_Reach_NLRI> getVpn4Nlries() {
		return vpn4Nlries;
	}

	public void setVpn4Nlries(List<VPN4_Reach_NLRI> vpn4Nlries) {
		this.vpn4Nlries = vpn4Nlries;
	}

	public InetAddress getNextHop() {
		return nextHop;
	}

	public void setNextHop(InetAddress nextHop) {
		this.nextHop = nextHop;
		if (nextHop instanceof Inet4Address){
    		nextHopLength=4;
    	} else if (nextHop instanceof Inet6Address){
    		nextHopLength=8;
    	} else {
    		nextHopLength=4;
    	}
	}
	
	public String toString() {
		//String sb = super.toString();
		StringBuffer ret = new StringBuffer(getVpn4Nlries().size() * 800);
		ret.append("MPLS VPN MP REACH ATTRIBUTE: NEXTHOP:"+getNextHop().getHostAddress()+"\n");
		for (int i=0; i<getVpn4Nlries().size();++i){
			ret.append("\t"+"> "+getVpn4Nlries().get(i).toString());
			if (i<getVpn4Nlries().size() ) ret.append("\n");
		}
		return ret.toString();
	}
	
}