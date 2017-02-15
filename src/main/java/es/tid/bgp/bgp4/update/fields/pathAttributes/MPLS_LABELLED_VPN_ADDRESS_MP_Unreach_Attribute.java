package es.tid.bgp.bgp4.update.fields.pathAttributes;


import java.util.LinkedList;
import java.util.List;

import es.tid.bgp.bgp4.update.fields.VPN4_Unreach_NLRI;




public class MPLS_LABELLED_VPN_ADDRESS_MP_Unreach_Attribute extends MP_Unreach_Attribute {
	
	private int totalNlriLength;
	    
    private VPN4_Unreach_NLRI vpn4nlri;

    private List<VPN4_Unreach_NLRI> vpn4Nlries;
    
	public MPLS_LABELLED_VPN_ADDRESS_MP_Unreach_Attribute(){
		super();
		this.setAddressFamilyIdentifier(1);
		this.setSubsequentAddressFamilyIdentifier(128);
	}
	
	public MPLS_LABELLED_VPN_ADDRESS_MP_Unreach_Attribute(byte [] bytes, int offset) {
		super(bytes, offset);
		vpn4Nlries = new LinkedList<VPN4_Unreach_NLRI>();
		if (this.extendedLengthBit == true) {
		this.totalNlriLength=((bytes[offset+2]&0xFF)<<8)|(bytes[offset+3]&0xFF);
		offset=offset+4;
		} else {
		this.totalNlriLength=(bytes[offset+2]&0xFF);
		offset=offset+3;
		}
		this.totalNlriLength=this.totalNlriLength-2-1; //remove afi, safi
		offset=offset+3; //move to the nlri
		int remainingNlriLength = this.totalNlriLength;
		while (remainingNlriLength>0) {
		VPN4_Unreach_NLRI tempvpn4nlri = new VPN4_Unreach_NLRI(bytes, offset);
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
	  	this.setPathAttributeLength(2+1); //afi+safi
		for(int i=0; i< vpn4Nlries.size() ;++i)
		{
			vpn4Nlries.get(i).encode();
			this.setPathAttributeLength( this.getPathAttributeLength()+vpn4Nlries.get(i).getLength());
		}
		// if total lenght is more than 255 setPathAttributeLength sets it as extended length automaticaly
		this.bytes=new byte[this.getLength()];
		encodeHeader();
	   	int offset = this.getMandatoryLength();
	  	//AFI
    	this.bytes[offset]=(byte)((this.getAddressFamilyIdentifier()>>8)&0xFF);
    	this.bytes[offset+1]=(byte)(this.getAddressFamilyIdentifier() &0xFF);
    	//SAFI
    	this.bytes[offset+2]=(byte)(this.getSubsequentAddressFamilyIdentifier() &0xFF);
        offset=offset+3;
		for(int i=0; i< vpn4Nlries.size() ;++i)
		{
			System.arraycopy(vpn4Nlries.get(i).getBytes(), 0, this.bytes, offset, vpn4Nlries.get(i).getLength());
			offset=offset+vpn4Nlries.get(i).getLength();
		}
	}

	public VPN4_Unreach_NLRI getVpn4nlri() {
		return vpn4nlri;
	}

	public void setVpn4nlri(VPN4_Unreach_NLRI vpn4nlri) {
		this.vpn4nlri = vpn4nlri;
	}

	public List<VPN4_Unreach_NLRI> getVpn4Nlries() {
		return vpn4Nlries;
	}

	public void setVpn4Nlries(List<VPN4_Unreach_NLRI> vpn4Nlries) {
		this.vpn4Nlries = vpn4Nlries;
	}
	public String toString() {
		//String sb = super.toString();
		StringBuffer ret = new StringBuffer(getVpn4Nlries().size() * 800);
		ret.append("MPLS VPN MP UNREACH ATTRIBUTE"+"\n");
		for (int i=0; i<getVpn4Nlries().size();++i){
			ret.append("\t"+"> "+getVpn4Nlries().get(i).toString());
			if (i<getVpn4Nlries().size() ) ret.append("\n");
		}
		return ret.toString();
	}
}