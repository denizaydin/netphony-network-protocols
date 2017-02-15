package es.tid.bgp.bgp4.update.fields.pathAttributes;

import es.tid.bgp.bgp4.update.fields.PathAttribute;

import java.util.LinkedList;
import java.util.List;


public class Community_Attribute extends PathAttribute
{
	private List<Community_Segment> Comms;

	public Community_Attribute()
	{
		super();
		//Poner los flags. 
		this.typeCode = PathAttributesTypeCode.PATH_ATTRIBUTE_TYPECODE_COMMUNITY;
		Comms = new LinkedList<Community_Segment>();
	}

	public List<Community_Segment> getComms() {
		return Comms;
	}

	public void setComms(List<Community_Segment> comms) {
		Comms = comms;
	}

	public Community_Attribute(byte[] bytes, int offset) 
	{
		super(bytes, offset);
		Comms = new LinkedList<Community_Segment>();
		decode();		
	}

	@Override
	public void encode()
	{
		int path_attribute_length=0;		
		for(Community_Segment community : Comms)
		{
			community.encode();
			path_attribute_length+=community.getLength();
		}
		this.setPathAttributeLength(path_attribute_length);
		
		bytes = new byte[length];
		this.optionalBit=true;
		encodeHeader();
		int offset = this.mandatoryLength;
		int num_segs=Comms.size();
		for(int i=0;i<num_segs;++i)
		{
			System.arraycopy(Comms.get(i).getBytes(), 0, bytes, offset, Comms.get(i).getLength());
			offset += Comms.get(i).getLength();
		}

	}

	public void decode()
	{
			int offset = this.mandatoryLength; 
			int numberofcomm = getPathAttributeLength()/4;
			int i=0;
			while(i < numberofcomm)
			{
				Community_Segment community = new Community_Segment(bytes, offset);
				Comms.add(community);
				offset += community.getLength();
				i++;
			}
	}

	public String toString() {
		//String sb = super.toString();
		String ret="";
		ret+="COMMUNITY [ ";
		for (int i=0; i<Comms.size();++i){
			ret+=Comms.get(i).toString()+" ";
		}
		ret+="]";
		return ret;
	}
}
