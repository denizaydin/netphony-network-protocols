package es.tid.tests;

import es.tid.bgp.bgp4.update.fields.LinkNLRI;
import es.tid.bgp.bgp4.update.fields.LinkStateNLRI;
import es.tid.bgp.bgp4.update.fields.NodeNLRI;
import es.tid.bgp.bgp4.update.fields.pathAttributes.BGP_LS_MP_Reach_Attribute;
import es.tid.bgp.bgp4.update.fields.pathAttributes.Generic_MP_Unreach_Attribute;
import es.tid.bgp.bgp4.update.fields.pathAttributes.MP_Unreach_Attribute;
import es.tid.bgp.bgp4.update.tlv.LocalNodeDescriptorsTLV;
import es.tid.protocol.commons.ByteHandler;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

import java.lang.reflect.Constructor;
import java.net.Inet4Address;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertTrue;

@RunWith(org.junit.runners.Parameterized.class)
public class TestBGPLSPathAttributes {

	String object;

	@Parameters(name="{0}")
	public static Collection<Object[]> configs() {
		Object[][] objects={
				{"es.tid.bgp.bgp4.update.fields.NodeNLRI"},
				//{"es.tid.bgp.bgp4.update.fields.LinkNLRI"}
		};
		return Arrays.asList(objects);
	}


	public TestBGPLSPathAttributes (String object) {
		this.object=object;
	}

	@Test
	public void test (){
		try{
			System.out.println("Testing BGP-LS  MP_Reach_Attribute with "+object);
			BGP_LS_MP_Reach_Attribute object2 = new BGP_LS_MP_Reach_Attribute();
			Class<?> objectClass=Class.forName(object2.getClass().getName());				
			//createAllFields(object);
			object2.setNextHop(Inet4Address.getByName("1.1.1.1"));
			object2.setNextHopLength(4);
			Class<?> objectClassNLRI=Class.forName(object);
			LinkStateNLRI ls = (LinkStateNLRI)objectClassNLRI.newInstance();  
			//TestPCEPCommons.createAllFields(ls);
			if (object.equals("es.tid.bgp.bgp4.update.fields.NodeNLRI")){
				NodeNLRI nodeNLRI =(NodeNLRI)ls;
				nodeNLRI.setProtocolID(1);
				nodeNLRI.setRoutingUniverseIdentifier(1);
				LocalNodeDescriptorsTLV ln= new LocalNodeDescriptorsTLV();
				nodeNLRI.setLocalNodeDescriptors(ln);
				TestPCEPCommons.createAllFields(ln);
			}else {
				LinkNLRI linkNLRI=(LinkNLRI)ls;
				//linkNLRI.s
				//TestPCEPCommons.createAllFields(ls);
				linkNLRI.setIdentifier(1);
				//linkNLRI.setIpv4InterfaceAddressTLV(Inet4Address.getByName("2.2.2.2"))
			}
			
			
			object2.setLsNLRI(ls);
			object2.encode();
			Constructor<?> ctor = objectClass.getConstructor(byte[].class,int.class);
			BGP_LS_MP_Reach_Attribute object3 = (BGP_LS_MP_Reach_Attribute) ctor.newInstance(object2.getBytes(),0);
			object3.encode();
			System.out.println(ByteHandler.ByteMACToString(object2.getBytes()));
			System.out.println(ByteHandler.ByteMACToString(object3.getBytes()));

			//Check if the fields are the same
			assertTrue("testing object "+objectClass,object2.equals(object3));
		} catch(Exception e){
			e.printStackTrace();
			assertTrue("Exception in object BGP-LS MP Reach Attribute",false);

		}
	}

	@Test
	public void testMP_Unreach_Attriute(){
		MP_Unreach_Attribute mu1 = new Generic_MP_Unreach_Attribute();
		mu1.encode();
		byte[] bytes1 = mu1.getBytes();

		MP_Unreach_Attribute mu2 = new Generic_MP_Unreach_Attribute(bytes1,0);
		byte[] bytes2 = mu2.getBytes();

		System.out.println(Arrays.toString(bytes1));
		System.out.println(Arrays.toString(bytes2));

		Assert.assertEquals("Both objects should be equal", mu1, mu2);
		Assert.assertTrue("Bytes from both objects should be the equal", Arrays.equals(bytes1, bytes2));


	}


}
