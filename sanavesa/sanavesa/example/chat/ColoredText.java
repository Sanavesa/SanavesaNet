package sanavesa.example.chat;

import javafx.scene.paint.Color;

/**
 * @author Mohammad Alali
 */
public class ColoredText
{
	private String text;
	private Color color;
	
	public ColoredText(String text, Color color)
	{
		this.text = text;
		this.color = color;
	}

	public String getText()
	{
		return text;
	}

	public Color getColor()
	{
		return color;
	}
}
