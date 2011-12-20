package com.android.ims.core.media;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CryptoParam {
	private int tag;
	private final String algorithm;
	private String key;
	private final List<String> params = new ArrayList<String>();

	public CryptoParam(int tag, String algorithm, String key) {
		this(tag, algorithm, key, null);
	}
	public CryptoParam(int tag, String algorithm, String key, String[] params) {
		this.tag = tag;
		this.algorithm = algorithm;
		this.key = key;
		
		if(params != null) {
			this.params.addAll(Arrays.asList(params)); 
		}
	}

	public int getTag() {
		return tag;
	}
	
	public void setTag(int tag) {
		this.tag = tag;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public String[] getParams() {
		return params.toArray(new String[0]);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((algorithm == null) ? 0 : algorithm.hashCode());
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((params == null) ? 0 : params.hashCode());
		result = prime * result + tag;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CryptoParam other = (CryptoParam) obj;
		if (algorithm == null) {
			if (other.algorithm != null)
				return false;
		} else if (!algorithm.equals(other.algorithm))
			return false;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (params == null) {
			if (other.params != null)
				return false;
		} else if (!params.equals(other.params))
			return false;
		if (tag != other.tag)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "CryptoParam [tag=" + tag + ", algorithm=" + algorithm
				+ ", key=" + key + ", params=" + params + "]";
	}
}
