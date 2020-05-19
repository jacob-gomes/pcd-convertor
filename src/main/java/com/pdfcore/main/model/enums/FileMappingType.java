/**
 * 
 */
package com.pdfcore.main.model.enums;

/**
 * @author tqn
 * 
 */
public enum FileMappingType {
	PROPERTIES("properties"), ROW_CSV("row_csv"), COL_CSV("col_csv"), UNNKOWN(
			"unnkown");
	private String fileType;

	/**
	 * 
	 */
	private FileMappingType(String fileType) {
		this.fileType = fileType;
	}

	public static FileMappingType parse(String fileType) {
		for (FileMappingType val : values()) {
			if (val.fileType.equalsIgnoreCase(fileType)) {
				return val;
			}
		}

		return UNNKOWN;

	}

}
