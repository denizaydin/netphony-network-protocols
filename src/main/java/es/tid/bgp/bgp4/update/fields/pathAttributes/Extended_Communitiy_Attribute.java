package es.tid.bgp.bgp4.update.fields.pathAttributes;

import es.tid.bgp.bgp4.update.fields.PathAttribute;

import java.util.LinkedList;
import java.util.List;
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
public  class Extended_Communitiy_Attribute  extends PathAttribute{
    
	private List<Extended_Communitiy_Segment> extComms;
		
	
	public Extended_Communitiy_Attribute()
	{
		super();
		//Poner los flags. 
		typeCode = PathAttributesTypeCode.PATH_ATTRIBUTE_TYPECODE_EXT_COMMUNITY;
		this.optionalBit=true;
		this.transitiveBit=true;
		extComms = new LinkedList<Extended_Communitiy_Segment>();

	}
	
	public Extended_Communitiy_Attribute(byte []bytes, int offset){
		super(bytes, offset);
		extComms = new LinkedList<Extended_Communitiy_Segment>();
		decode();

	}
	
	public List<Extended_Communitiy_Segment> getExtComms() {
		return extComms;
	}

	public void setExtComms(List<Extended_Communitiy_Segment> extComms) {
		this.extComms = extComms;
	}
   public void decode() {
		int offset = this.mandatoryLength; 
		int numberofcomm = getPathAttributeLength()/8;
		int i=0;
		while(i < numberofcomm)
		{
			Extended_Communitiy_Segment extComm = new Extended_Communitiy_Segment(bytes, offset);
			extComms.add(extComm);
			offset += extComm.getLength();
			i++;
		}
   }
	public void encode() {
		int path_attribute_length=0;		
		for(Extended_Communitiy_Segment extComm : extComms)
		{
			extComm.encode();
			path_attribute_length+=extComm.getLength();
		}
		this.setPathAttributeLength(path_attribute_length);
		
		bytes = new byte[length];
		encodeHeader();
		int offset = this.mandatoryLength; //After the header encoding
		int num_segs=extComms.size();
		for(int i=0;i<num_segs;++i)
		{

			System.arraycopy(extComms.get(i).getBytes(), 0, bytes, offset, extComms.get(i).getLength());
			offset += extComms.get(i).getLength();
		}
	}

	public String toString() {
		String ret="";
		ret+="EXTENDED COMMUNITY [ ";
		for (int i=0; i<extComms.size();++i){
			ret+=extComms.get(i).toString()+" ";
		}
		ret+="]";
		return ret;
	}
}
