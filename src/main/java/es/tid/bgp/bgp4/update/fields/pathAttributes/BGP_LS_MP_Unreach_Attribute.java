package es.tid.bgp.bgp4.update.fields.pathAttributes;

import es.tid.bgp.bgp4.update.fields.*;

import java.util.LinkedList;
import java.util.List;



/**
 *
 *  @author Deniz AYDIN
 *
 */

public class BGP_LS_MP_Unreach_Attribute extends MP_Unreach_Attribute {

	private LinkStateNLRI lsNLRI;
	private List<LinkStateNLRI> lsNLRIList;

	public BGP_LS_MP_Unreach_Attribute(){
		super();
		this.setAddressFamilyIdentifier(AFICodes.AFI_BGP_LS);
		this.setSubsequentAddressFamilyIdentifier(SAFICodes.SAFI_BGP_LS);
		lsNLRIList = new LinkedList<LinkStateNLRI>();
	}

	public BGP_LS_MP_Unreach_Attribute(byte [] bytes, int offset) {
		super(bytes, offset);
		int offset2=offset+this.mandatoryLength+3;
		lsNLRIList = new LinkedList<LinkStateNLRI>();
		while(offset2 < length+offset)
		{
			
			LinkStateNLRI tempNLri;

			int type = LinkStateNLRI.getType(bytes, offset2);
			

			if(type == NLRITypes.Link_NLRI)
			{
				tempNLri = new LinkNLRI(bytes, offset2);

			} else if(type == NLRITypes.Node_NLRI)
			{
				tempNLri = new NodeNLRI(bytes, offset2);

			} else if(type == NLRITypes.Prefix_v4_NLRI)
			{
				tempNLri = new PrefixNLRI(bytes, offset2);
			} else
			{
				continue;
			}
			offset2 += tempNLri.getTotalNLRILength();
			lsNLRIList.add(tempNLri);

		}
		lsNLRI = lsNLRIList.isEmpty() ? null : lsNLRIList.get(0);
	}

	public void encode() {
		//Encoding BGP_LS_MP_Reach_Attribute
//		this.pathAttributeLength = 5+lsNLRI.getLength()+this.getLengthofNextHopNetworkAddress();
		this.pathAttributeLength = 4;
		for(int i=0; i< lsNLRIList.size() ;++i)
		{
			lsNLRIList.get(i).encode();
			this.pathAttributeLength += lsNLRIList.get(i).getTotalNLRILength();
		}

		this.setPathAttributeLength(pathAttributeLength);
		this.bytes=new byte[this.getLength()];
		encodeHeader();
		encodeMP_Unreach_Header();
		int offset = this.getMandatoryLength()+4;
		for(int i=0; i< lsNLRIList.size() ;++i)
		{
			System.arraycopy(lsNLRIList.get(i).getBytes(), 0, this.bytes, offset, lsNLRIList.get(i).getTotalNLRILength());
			offset += lsNLRIList.get(i).getTotalNLRILength();
		}

	}

	public LinkStateNLRI getLsNLRI() {
		return lsNLRI;
	}
	public List<LinkStateNLRI> getLsNLRIList(){ return lsNLRIList; }

	public void setLsNLRI(LinkStateNLRI lsNLRI) {
		this.lsNLRI = lsNLRI;
		if (this.lsNLRIList.size()>0){
			this.lsNLRIList=new LinkedList<LinkStateNLRI>();
		}		
		this.lsNLRIList.add(lsNLRI);
	}
	public void setLsNLRIList(List<LinkStateNLRI> lsNLRIList){ this.lsNLRIList = lsNLRIList; }

	public String toString(){
		StringBuilder sb = new StringBuilder("[BGP_LS_MP_UNREACH \n");
		for(LinkStateNLRI ls : lsNLRIList)
			sb.append(ls.toString()+" ");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		for(LinkStateNLRI ls :lsNLRIList)
			result = prime * result + lsNLRI.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		BGP_LS_MP_Unreach_Attribute other = (BGP_LS_MP_Unreach_Attribute) obj;
		if(lsNLRIList.size() != other.getLsNLRIList().size())
			return false;
		List<LinkStateNLRI> otherlsNLRIList = other.getLsNLRIList();
		if(!lsNLRIList.equals(otherlsNLRIList))
			return false;
		return true;
	}





}
