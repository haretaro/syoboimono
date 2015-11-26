package com.github.haretaro.syoboimono.domutility;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NodeListIterator implements Iterator<Node> {
	private NodeList nodeList;
	private int index = 0;

	public NodeListIterator(NodeList nodeList){
		this.nodeList = nodeList;
	}
	
	@Override
	public boolean hasNext() {
		return index < nodeList.getLength();
	}

	@Override
	public Node next() {
		Node temp = nodeList.item(index);
		if(temp == null){
			throw new NoSuchElementException();
		}
		index ++;
		return temp;
	}

}
