package ca.pfv.spmf.algorithms.frequentpatterns.eclat_and_charm;
/* This file is copyright (c) 2008-2013 Philippe Fournier-Viger
* 
* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
* 
* SPMF is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
* 
* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.pfv.spmf.patterns.itemset_set_integers_with_tids.Itemset;

/**
 * This class represents an ITNode from the ITSearch Tree 
 * used by Charm and Eclat algorithms.<br/><br/>
 * 
 * @see ITSearchTree
 * @see AlgoCharm
 * @see AlgoEclat
 * @see Itemset
 * @author Philippe Fournier-Viger
 */
class ITNode {
	// the itemset stored in that node
	private Itemset itemset;
	// the tidset associated to that node
	private Set<Integer> tidset;

	// the parent node of that node
	private ITNode parent = null;
	// the child nodes of that node
	private List<ITNode> childNodes = new ArrayList<ITNode>();

	/**
	 * Constructor of the node.
	 * @param itemset the itemset for that node.
	 */
	public ITNode(Itemset itemset) {
		this.itemset = itemset;
	}

	/**
	 * Get the itemset of that node.
	 * @return an Itemset.
	 */
	public Itemset getItemset() {
		return itemset;
	}

	/**
	 * Set the itemset of that node.
	 * @param itemset an itemset.
	 */
	public void setItemset(Itemset itemset) {
		this.itemset = itemset;
	}

	/**
	 * Get the tidset of that node
	 * @return the tidset as a Set of Integers.
	 */
	public Set<Integer> getTidset() {
		return tidset;
	}

	/**
	 * Set the tidset of that node.
	 * @param tidset 
	 */
	public void setTidset(Set<Integer> tidset) {
		this.tidset = tidset;
	}

	/**
	 * Get the child nodes of this node
	 * @return a list of ITNodes.
	 */
	public List<ITNode> getChildNodes() {
		return childNodes;
	}

	/**
	 * Set the child node of this node.
	 * @param childNodes a list of nodes.
	 */
	public void setChildNodes(List<ITNode> childNodes) {
		this.childNodes = childNodes;
	}

	/**
	 * Get the parent of this node
	 * @return a node or null if no parent.
	 */
	public ITNode getParent() {
		return parent;
	}

	/**
	 * Set the parent of this node to a given node.
	 * @param parent the given node.
	 */
	public void setParent(ITNode parent) {
		this.parent = parent;
	}

	/**
	 * Method used by Charm to replace all itemsets in the subtree defined
	 * by this node as the itemsets union a replacement itemset.
	 * @param replacement the replacement itemset
	 */
	void replaceInChildren(Itemset replacement) {
		// for each child node
		for (ITNode node : getChildNodes()) {
			// get the itemset of the child node
			Itemset itemset = node.getItemset();
			// could be optimized... not very efficient..
			// in particular, instead of using a list in itemset, we could use a
			// set.
			
			// for each item in the replacement
			for (Integer item : replacement.getItems()) {
				// if it is not in the itemset already
				if (!itemset.contains(item)) {
					// add it
					itemset.addItem(item);
				}
			}
			// recursive call for the children of the current node
			node.replaceInChildren(replacement);
		}
	}

	/**
	 * Perform tidset intersection with the tidset of a brother node
	 * @param brother the brother node
	 * @return  the resulting tidset of null if it was determined that the resulting tidset
	 *     would have less tids than minsup
	 */
	 Set<Integer> peformOptimizedTidSetsIntersection(ITNode brother, int minsupRelative) {
		// NEW OPTIMIZATION - 2014  
		// To perform the tidset intersection, we compare the itemset with the smallest tidset
		// against the one having the larger tidset.  Furthermore, we stop calculating the
		// tidset intersection if too many tids are not common so that minsup could not be attained if
		// we continue.
		// THIS OPTIMIZATION IS ESPECIALLY EFFECTIVE FOR SPARSE DATASETS AND LOW MINSUP VALUES
		// E.g.  on retail dataset,  up to 50 % faster for minsup = 0.0001
		int brotherSize = brother.getTidset().size();
		int thisNodeSize = this.getTidset().size();
		
		// create list of common tids of the itemset of the current node
		// and the brother node
		Set<Integer> commonTids = new HashSet<Integer>();
					
		if(brotherSize < thisNodeSize){
			// for each tid in the tidset of the current node
			for (Integer tid : brother.getTidset()) {
				// if it is in the tidset of the brother node
				if (this.getTidset().contains(tid)) {
					// add it to the set of common tids
					commonTids.add(tid);
				}
				// decrement the number of tids left
				brotherSize--;
				// if it is not possible to have a frequent itemset giving the number of tids left, then stop
				if(brotherSize + commonTids.size() < minsupRelative) {
					return null;
				}
			}
		}else {
			// for each tid in the tidset of the current node
			for (Integer tid : this.getTidset()) {
				// if it is in the tidset of the brother node
				if (brother.getTidset().contains(tid)) {
					// add it to the set of common tids
					commonTids.add(tid);
				}
				// decrement the number of tids left
				thisNodeSize--;
				// if it is not possible to have a frequent itemset giving the number of tids left, then stop
				if(thisNodeSize + commonTids.size() < minsupRelative) {
					return null;
				}
			}
		}
		return commonTids;
	}

//	public boolean hasSameItemsetAs(ITNode brother) {
//		return brother.getItemset().itemset.size() == this.getItemset().itemset.size()
//				&& brother.getItemset().itemset.containsAll(this.getItemset().itemset);
//	}

}
