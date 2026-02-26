package com.creati.ui.components;

import com.creati.ui.main.MainUiParts;
import com.creati.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public class ToggleChipGroup extends JPanel {

	public static class Item {
		public final String label;
		public final int iconCodePoint;
		public Item(String label, int iconCodePoint) {
			this.label = label;
			this.iconCodePoint = iconCodePoint;
		}
	}

	private final ButtonGroup group = new ButtonGroup();
	private final List<ToggleChip> chips = new ArrayList<>();
	private final List<Consumer<String>> listeners = new ArrayList<>();
	private Runnable onSelectionChanged;

	public ToggleChipGroup(Item[] items) {
		super(new FlowLayout(FlowLayout.LEFT, 10, 8));
		setOpaque(false);
		for (Item it : items) addItem(it);
	}

	public void addSelectionListener(Consumer<String> l) { if (l != null) listeners.add(l); }

	
	public boolean containsLabel(String label) {
		if (label == null) return false;
		for (ToggleChip c : chips) if (label.equals(c.getText())) return true;
		return false;
	}

	public String getSelectedText() { return getSelectedLabel(); }

	public void selectByText(String text) { setSelectedLabel(text); }

	public void setOnSelectionChanged(Runnable r) { this.onSelectionChanged = r; }

	public String getSelectedLabel() {
		for (ToggleChip c : chips) if (c.isSelected()) return c.getText();
		return null;
	}

	public void setSelectedLabel(String label) {
		if (label == null) { clearSelection(); return; }
		for (ToggleChip c : chips) {
			if (label.equals(c.getText())) { c.setSelected(true); return; }
		}
	}

	public void clearSelection() { group.clearSelection(); repaint(); }

	public int getSelectedIndex() {
		for (int i = 0; i < chips.size(); i++) if (chips.get(i).isSelected()) return i;
		return -1;
	}

	private void addItem(Item it) {
		Icon ic = MainUiParts.glyphIcon(it.iconCodePoint, 18f, UITheme.ICON_MUTED);
		ToggleChip chip = new ToggleChip(it.label, ic);
		chips.add(chip);
		group.add(chip);
		add(chip);

		chip.addActionListener(e -> {
			String sel = getSelectedLabel();
			for (Consumer<String> l : listeners) l.accept(sel);
			if (onSelectionChanged != null) onSelectionChanged.run();
		});
	}
}
