package de.slopjong.erwiz.dot;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.slopjong.erwiz.common.ResourceUtils;
import de.slopjong.erwiz.dot.ResourceName;
import de.slopjong.erwiz.model.ColorPair;
import de.slopjong.erwiz.model.Entity;
import de.slopjong.erwiz.model.ErdNotation;
import de.slopjong.erwiz.model.Model;
import de.slopjong.erwiz.model.Relationship;


import static de.slopjong.erwiz.dot.PackageUtils.escapeLabel;

/**
 * This class is a generator of a string to write dot files.
 * This class is used by other classes which are defined in other packages.
 * 
 * @author kono
 * @version 1.0
 */
public final class GraphGenerator {
	
	private final Model model;
	private final ErdNotation notation;
	private final String fontName;
	private final ColorPair colorPair;
	private final RankDirection rankDir = RankDirection.LEFT_TO_RIGHT; //fixed as of this version
	private StringBuilder sb;

	private static final String INDENT1 = "\t";
	private static final String INDENT2 = "\t\t";
	
	/**
	 * Creates an instance of this class.
	 * 
	 * @param model model deta to generate dot files
	 * @param notation the ERD notation
	 * @param colorPair the color pair
	 */
	public GraphGenerator(Model model, ErdNotation notation, 
			String fontName, ColorPair colorPair) {
		this.model = model;
		this.notation = notation;
		this.fontName = fontName;
		this.colorPair = colorPair;
	}
	
	/**
	 * Generates a string for the specified model.
	 * 
	 * @return the generated string
	 * @throws IOException if resource error has occured
	 */
	public String execute() throws IOException {
		this.sb = new StringBuilder();
		
		//header comment
		final String hc = generateHeaderCommentLine();
		this.sb.append("/* \n");
		this.sb.append(" * ").append(hc).append("\n");
		this.sb.append(" */\n");
		this.sb.append("\n");
		
		//begin graph
		this.sb.append("digraph ERD {\n");
		this.sb.append("\n");
		
		//global
		this.sb.append(generateGlobalAttributes(ResourceName.GraphAttributes));
		this.sb.append(generateGlobalAttributes(ResourceName.NodeAttributes));
		this.sb.append(generateGlobalAttributes(ResourceName.EdgeAttributes));
		
		//generator for entities and relationships
		final ERGenerator gen = FactoryMethods.createERGenerator(
				this.notation, RankDirection.LEFT_TO_RIGHT, this.model.getOptions());
		
		//entities
		for (Entity entity : this.model.getEntityList()) {
			final List<String> lines = gen.generateEntityLines(entity);
			
			for (String line : lines) {
				this.sb.append(INDENT1).append(line).append("\n");
			}
			this.sb.append("\n");
		}
		
		//relationships
		for (Relationship rel : this.model.getRelationshipList()) {
			final Map<String, Entity> map = this.model.getEntityMap();
			final String eid1 = map.get(rel.getNameOfEntity1()).getId();
			final String eid2 = map.get(rel.getNameOfEntity2()).getId();
			
			final List<String> lines = gen.generateRelLines(rel, eid1, eid2);
			
			for (String line : lines) {
				this.sb.append(INDENT1).append(line).append("\n");
			}
			this.sb.append("\n");
		}
		
		//end graph
		this.sb.append("}\n");
		
		return this.sb.toString();
	}
	
	private String generateHeaderCommentLine() {
		final String appName = ResourceUtils.readAppNameText();
		
		final String dtFmt = "yyyy/MM/dd HH:mm:ss.SSS Z";
		final String datetime = new SimpleDateFormat(dtFmt).format(new Date());
		
		final String msgFmt = "This file was generated by [%s] at [%s]";
		return String.format(msgFmt, appName, datetime);
	}
	
	private String generateGlobalAttributes(ResourceName resource) throws IOException {
		
		List<String> confLines = ResourceUtils.readResouceLines(resource.getPath(), resource.getClass());
		StringBuilder sb = new StringBuilder();
		
		//begin
		sb.append(INDENT1).append(resource.getName()).append(" [\n");
		
		//write lines
		for (String line : confLines) {
			sb.append(INDENT2).append(line).append("\n");
		}
		
		//dynamic data
		switch (resource) {
			case GraphAttributes: {
				if (this.fontName != null) {
					sb.append(INDENT2).append("fontname=\"").append(this.fontName).append("\"\n");
				}
				String line = INDENT2 + "rankdir=" + this.rankDir.getValue() + "\n";
				sb.append(line);
				
				String title = this.model.getOptions().getString(OptionName.TITLE);
				if (!title.equals("")) {
					title = escapeLabel(title) + "\\n\\n";
				}
				line = INDENT2 + "label=\"" + title + "\"\n";
				sb.append(line);
				
				final int titleSize = this.model.getOptions().getInteger(OptionName.TITLE_SIZE);
				line = INDENT2 + "fontsize=" + titleSize + "\n";
				sb.append(line);
				
				break;
			}
			case NodeAttributes:{
				final String dark = this.colorPair.getDarkColor();
				final String light = this.colorPair.getLightColor();
				
				if (this.fontName != null) {
					sb.append(INDENT2).append("fontname=\"").append(this.fontName).append("\"\n");
				}
				sb.append(INDENT2).append("color=\"").append(dark).append("\"\n");
				sb.append(INDENT2).append("fillcolor=\"").append(light).append("\"\n");
				break;
			}
			case EdgeAttributes:{
				if (this.fontName != null) {
					sb.append(INDENT2).append("fontname=\"").append(this.fontName).append("\"\n");
				}
				break;
			}
			default:{
				assert false : "unknown dot resource : [" + resource + "]";
				break;
			}
		}
		
		//end
		sb.append(INDENT1).append("]\n");
		sb.append("\n");
		
		return sb.toString();
	}
	
}
