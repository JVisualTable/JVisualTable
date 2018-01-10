package org.vt.beans;

/**
 * search condition
 * 
 * @author Zerg Law
 * 
 */
public class SearchCondition {

    private boolean isAllColumnSearch;
    private int[] searchIndexs;
    private String searchTxt;

    public SearchCondition(boolean isAllColumnSearch, int[] searchIndexs,
	    String searchTxt) {
	super();
	this.isAllColumnSearch = isAllColumnSearch;
	this.searchIndexs = searchIndexs;
	this.searchTxt = searchTxt;
    }

    public boolean isAllColumnSearch() {
	return isAllColumnSearch;
    }

    public void setAllColumnSearch(boolean isAllColumnSearch) {
	this.isAllColumnSearch = isAllColumnSearch;
    }

    public int[] getSearchIndexs() {
	return searchIndexs;
    }

    public void setSearchIndex(int[] searchIndexs) {
	this.searchIndexs = searchIndexs;
    }

    public String getSearchTxt() {
	return searchTxt;
    }

    public void setSearchTxt(String searchTxt) {
	this.searchTxt = searchTxt;
    }
}
