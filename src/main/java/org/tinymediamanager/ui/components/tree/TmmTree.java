/*
 * Copyright 2012 - 2017 Manuel Laggner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tinymediamanager.ui.components.tree;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.UIResource;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.jgoodies.forms.layout.FormLayout;

/**
 * The class TmmTree is our base class for an enhanced JTree
 * 
 * @author Manuel Laggner
 *
 * @param <E>
 *          the node type
 */
public class TmmTree<E extends TmmTreeNode> extends JTree {
  private static final long        serialVersionUID = 4918691644082882866L;

  protected Set<ITmmTreeFilter<E>> treeFilters;
  protected PropertyChangeListener filterChangeListener;

  /**
   * create a new tree for the given data provider
   * 
   * @param dataProvider
   *          the data provider for this tree
   */
  public TmmTree(TmmTreeDataProvider<E> dataProvider) {
    super();
    treeFilters = new HashSet<>();
    filterChangeListener = evt -> updateFiltering();
    setOpaque(false);
    setDataProvider(dataProvider);
  }

  /**
   * get the data provider for this tree
   * 
   * @return the data provider used in this tree
   */
  @SuppressWarnings("unchecked")
  public TmmTreeDataProvider<E> getDataProvider() {
    final TreeModel model = getModel();
    return model != null && model instanceof TmmTreeModel ? ((TmmTreeModel<E>) model).getDataProvider() : null;
  }

  /**
   * get the data provider for this tree
   * 
   * @param dataProvider
   *          the data provider to be set
   */
  public void setDataProvider(final TmmTreeDataProvider<E> dataProvider) {
    if (dataProvider != null) {
      // add a reference to this filter set into the data provider for easier filtering
      dataProvider.setTreeFilters(treeFilters);

      // final TmmTreeDataProvider<E> oldDataProvider = getDataProvider();

      // Updating model
      // Be aware that all the data will be loaded right away
      setModel(new TmmTreeModel<>(this, dataProvider));

      // Informing about data provider change
      // firePropertyChange(TREE_DATA_PROVIDER_PROPERTY, oldDataProvider, dataProvider);
    }
  }

  /**
   * Returns all set tree nodes filter.
   *
   * @return a list of all set tree nodes filters
   */
  public List<ITmmTreeFilter<E>> getFilters() {
    return new ArrayList<ITmmTreeFilter<E>>(treeFilters);
  }

  /**
   * Removes any applied tree nodes filter.
   */
  public void clearFilter() {
    // remove our filter listener
    for (ITmmTreeFilter<E> filter : treeFilters) {
      filter.removePropertyChangeListener(filterChangeListener);
    }

    treeFilters.clear();
    updateFiltering();
  }

  /**
   * add a new filter to this tree
   * 
   * @param newFilter
   *          the new filter to be added
   */
  public void addFilter(ITmmTreeFilter<E> newFilter) {
    // add our filter listener
    newFilter.addPropertyChangeListener(ITmmTreeFilter.TREE_FILTER_CHANGED, filterChangeListener);

    treeFilters.add(newFilter);
    updateFiltering();
  }

  /**
   * removes the given filter from this tree
   * 
   * @param filter
   *          the filter to be removed
   */
  public void removeFilter(ITmmTreeFilter<E> filter) {
    // remove our filter listener
    filter.removePropertyChangeListener(filterChangeListener);

    treeFilters.remove(filter);
    updateFiltering();
  }

  /**
   * Updates nodes sorting and filtering for all loaded nodes.
   */
  @SuppressWarnings("unchecked")
  public void updateFiltering() {
    final TreeModel model = getModel();
    if (model != null && model instanceof TmmTreeModel) {
      ((TmmTreeModel<E>) getModel()).updateSortingAndFiltering();
    }
  }

  /**
   * Get the tree state (nodes and expanded states)
   * 
   * @return the state of this tree
   */
  public TmmTreeState getTreeState() {
    final Object root = getModel().getRoot();

    final TmmTreeState treeState = new TmmTreeState();
    final List<TmmTreeNode> elements = new ArrayList<>();
    elements.add((TmmTreeNode) root);
    while (elements.size() > 0) {
      final TmmTreeNode element = elements.get(0);
      final TreePath path = new TreePath(element.getPath());
      treeState.addState(element.getId(), isExpanded(path), isPathSelected(path));

      for (int i = 0; i < element.getChildCount(); i++) {
        elements.add((TmmTreeNode) element.getChildAt(i));
      }

      elements.remove(element);
    }
    return treeState;
  }

  /**
   * Set the tree state (nodes and expanded states)
   * 
   * @param treeState
   *          the state to be set
   */
  public void setTreeState(TmmTreeState treeState) {
    final Object root = getModel().getRoot();

    if (treeState == null) {
      return;
    }

    clearSelection();

    final List<TmmTreeNode> elements = new ArrayList<>();
    elements.add((TmmTreeNode) root);
    while (elements.size() > 0) {
      final TmmTreeNode element = elements.get(0);
      final TreePath path = new TreePath(element.getPath());

      // Restoring expansion states
      if (treeState.isExpanded(element.getId())) {
        expandPath(path);

        // We are going futher only into expanded nodes, otherwise this will expand even collapsed ones
        for (int i = 0; i < element.getChildCount(); i++) {
          elements.add((TmmTreeNode) getModel().getChild(element, i));
        }
      }
      else {
        collapsePath(path);
      }

      // Restoring selection states
      if (treeState.isSelected(element.getId())) {
        addSelectionPath(path);
      }
      else {
        removeSelectionPath(path);
      }

      elements.remove(element);
    }
  }

  /*
   * helper classes
   */
  public static class BottomBorderBorder extends AbstractBorder implements UIResource {
    private static final long  serialVersionUID = -1431631265848685069L;
    public static final Color  COLOR            = new Color(211, 211, 211);
    private static final Color COLOR2           = new Color(248, 248, 248);

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      Graphics2D g2d = (Graphics2D) g;

      g.setColor(COLOR);
      g.drawLine(g.getClipBounds().x, height - 2, g.getClipBounds().width, height - 2);
      g.setColor(COLOR2);

      Composite savedComposite = g2d.getComposite();
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
      g.drawLine(g.getClipBounds().x, height - 1, g.getClipBounds().width, height - 1);

      g2d.setComposite(savedComposite);
    }
  }

  static class VerticalBorderPanel extends JPanel {
    private static final long serialVersionUID = -3832845181622979771L;
    private final Color       gridColor        = new Color(217, 217, 217);
    private List<Integer>     columnsWithoutBorder;

    public VerticalBorderPanel(int[] columnsWithoutBorder) {
      this.columnsWithoutBorder = new ArrayList<Integer>();
      for (int index = 0; index < columnsWithoutBorder.length; index++) {
        this.columnsWithoutBorder.add(columnsWithoutBorder[index]);
      }
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      LayoutManager layout = getLayout();
      if (!(layout instanceof FormLayout)) {
        return;
      }
      FormLayout formLayout = (FormLayout) layout;
      FormLayout.LayoutInfo layoutInfo = formLayout.getLayoutInfo(this);
      int height = layoutInfo.getHeight();

      g.setColor(this.gridColor);

      for (int row = 0; row <= layoutInfo.rowOrigins.length - 1; row++) {
        for (int col = 0; col <= layoutInfo.columnOrigins.length - 1; col++) {
          if (columnsWithoutBorder.contains(col)) {
            continue;
          }

          int x = layoutInfo.columnOrigins[col];
          int y = layoutInfo.rowOrigins[row];
          g.drawLine(x, y, x, y + height);
        }
      }
    }
  }
}