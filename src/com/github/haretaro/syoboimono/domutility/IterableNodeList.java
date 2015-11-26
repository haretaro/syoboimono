package com.github.haretaro.syoboimono.domutility;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class IterableNodeList implements NodeList, Iterable<Node> {
	private NodeList nodeList;
	
	public Stream<Node> stream(){
		return StreamSupport.stream(
				Spliterators.spliteratorUnknownSize(
						iterator(),
						Spliterator.ORDERED
				),
				false
		);
	}
	
	public IterableNodeList(NodeList nodeList){
		this.nodeList = nodeList;
	}

	@Override
	public Iterator<Node> iterator() {
		return new NodeListIterator(nodeList);
	}
	
	public Node item(int index){
		return nodeList.item(index);
	}
	
	public int getLength(){
		return nodeList.getLength();
	}

}
