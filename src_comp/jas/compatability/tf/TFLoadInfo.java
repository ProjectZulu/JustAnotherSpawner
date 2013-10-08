package jas.compatability.tf;

import java.util.ArrayList;
import java.util.List;

import jas.compatability.LoadInfo;

public class TFLoadInfo implements LoadInfo {

    @Override
    public List<String> getRequiredModIDs() {
        List<String> list = new ArrayList<String>();
        list.add("TwilightForest");
        return list;
    }

    @Override
    public List<Object> getObjectsToRegister() {
        List<Object> list = new ArrayList<Object>();
        list.add(new StructureInterpreterTwilightForest());
        return list;
    }

	@Override
	public String loaderID() {
		return "TwilightForest";
	}
}