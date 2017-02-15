package es.tid.bgp.bgp4.update.fields.pathAttributes;

import es.tid.bgp.bgp4.update.fields.PathAttribute;


/**
 LOCAL_PREF is a well-known attribute that is a four-octet
            unsigned integer.  A BGP speaker uses it to inform its other
            internal peers of the advertising speaker's degree of
            preference for an advertised route.                            
 * @author denizaydin
 *
 */
public class Local_Pref_Attribute extends PathAttribute{
	/**
	 * Local Preference value, default is 100
	 */
	int value;
	public Local_Pref_Attribute(){		
		super();
		//Poner los flags. 		
		this.typeCode = PathAttributesTypeCode.PATH_ATTRIBUTE_TYPECODE_LOCAL_PREF;
		this.value=100;

	}
	public Local_Pref_Attribute(byte []bytes, int offset){
		super(bytes, offset);
		decode(bytes,offset+mandatoryLength);		
	}
	@Override
	public void encode() {
		pathAttributeLength = 4;
		this.length=pathAttributeLength+mandatoryLength;
		this.bytes=new byte[this.length];
		encodeHeader();
		bytes[3] = (byte) ((value >> 24) & 0xFF);
		bytes[4] =  (byte) ((value >> 16) & 0xFF);
		bytes[5] =  (byte) ((value >> 8) & 0xFF);
		bytes[6] = (byte) (value & 0xFF);
	}
	public void decode(byte []bytes, int offset){
		value = ((bytes[offset]&0xFF)<<24) | ((bytes[offset+1]&0xFF)<<16) | ((bytes[offset+2]&0xFF)<<8) |  (bytes[offset+3]&0xFF) ;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		//String sb = super.toString();
		return "Local Pref [Value=" + value+ "]";
	}
	

}
