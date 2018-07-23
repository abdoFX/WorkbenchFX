package com.dlsc.workbenchfx.view;

import com.dlsc.workbenchfx.Workbench;
import com.dlsc.workbenchfx.model.WorkbenchDialog;
import com.dlsc.workbenchfx.util.WorkbenchUtils;
import com.dlsc.workbenchfx.view.controls.GlassPane;
import com.dlsc.workbenchfx.view.controls.dialog.DialogControl;
import java.util.Objects;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents the presenter of the corresponding {@link WorkbenchView}.
 *
 * @author François Martin
 * @author Marco Sanfratello
 */
public class WorkbenchPresenter extends Presenter {
  private static final Logger LOGGER =
      LogManager.getLogger(WorkbenchPresenter.class.getName());

  private Workbench model;
  private WorkbenchView view;

  private final ObservableMap<Node, GlassPane> overlays;
  private final ObservableSet<Node> overlaysShown;
  private final ObservableSet<Node> blockingOverlaysShown;

  /**
   * Constructs a new {@link WorkbenchPresenter} for the {@link WorkbenchView}.
   *
   * @param model the model of WorkbenchFX
   * @param view  corresponding view to this presenter
   */
  public WorkbenchPresenter(Workbench model, WorkbenchView view) {
    this.model = model;
    this.view = view;
    overlays = model.getOverlays();
    overlaysShown = model.getNonBlockingOverlaysShown();
    blockingOverlaysShown = model.getBlockingOverlaysShown();
    init();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void initializeViewParts() {
    view.contentView.setContent(view.homeView);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setupEventHandlers() {

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setupValueChangedListeners() {
    // When the active module changes, the new view is set to the home screen if null.
    model.activeModuleViewProperty().addListener((observable, oldModule, newModule) ->
        view.contentView.setContent(Objects.isNull(newModule) ? view.homeView : newModule)
    );

    overlays.addListener((MapChangeListener<Node, GlassPane>) c -> {
      LOGGER.trace("Listener overlays fired");
      if (c.wasAdded()) {
        LOGGER.trace("Overlay added");
        addOverlay(c.getKey(), c.getValueAdded());
      } else if (c.wasRemoved()) {
        LOGGER.trace("Overlay removed");
        removeOverlay(c.getKey(), c.getValueRemoved());
      }
    });

    WorkbenchUtils.addSetListener(
        overlaysShown,
        change -> showOverlay(change.getElementAdded(), false),
        change -> hideOverlay(change.getElementRemoved())
    );

    WorkbenchUtils.addSetListener(
        blockingOverlaysShown,
        change -> showOverlay(change.getElementAdded(), true),
        change -> hideOverlay(change.getElementRemoved())
    );
  }

  /**
   * Adds an {@code overlay} together with the {@code glassPane} to the view.
   *
   * @param overlay   to be added
   * @param glassPane to be added
   */
  public void addOverlay(Node overlay, GlassPane glassPane) {
    LOGGER.trace("addOverlay");
    view.addOverlay(overlay, glassPane);
  }

  /**
   * Removes an {@code overlay} together with the {@code glassPane} from the view.
   *
   * @param overlay   to be removed
   * @param glassPane to be removed
   */
  public void removeOverlay(Node overlay, GlassPane glassPane) {
    LOGGER.trace("removeOverlay");
    view.removeOverlay(overlay, glassPane);

    // invalidate previous event handler, if existent (when blocking)
    glassPane.setOnMouseClicked(null);
  }

  /**
   * Makes the {@code overlay} visible, along with its {@code glassPane}.
   *
   * @param overlay  to be shown
   * @param blocking if false, will make {@code overlay} hide, if its {@code glassPane} was clicked
   */
  public void showOverlay(Node overlay, boolean blocking) {
    showOverlay(overlay, overlays.get(overlay), blocking);
  }

  /**
   * Makes the {@code overlay} visible, along with its {@code glassPane}.
   *
   * @param overlay   to be shown
   * @param glassPane the {@code overlay}'s corresponding {@link GlassPane}
   * @param blocking if false, will make {@code overlay} hide, if its {@code glassPane} was clicked
   */
  private void showOverlay(Node overlay, GlassPane glassPane, boolean blocking) {
    LOGGER.trace("showOverlay - Blocking: " + blocking);
    view.showOverlay(overlay);

    // if overlay is not blocking, make the overlay hide when the glass pane is clicked
    if (!blocking) {
      LOGGER.trace("showOverlay - Set GlassPane EventHandler");
      glassPane.setOnMouseClicked(event -> {
        // check if overlay is really not blocking, is needed to avoid false-positives
        if (overlaysShown.contains(overlay)) {
          LOGGER.trace("GlassPane was clicked, hiding overlay");

          // if the overlay is a dialog
          if (overlay instanceof DialogControl) {
            LOGGER.trace("GlassPane was clicked, hiding dialog");
            WorkbenchDialog dialog = ((DialogControl) overlay).getDialog();
            dialog.getOnResult().accept(ButtonType.CANCEL);
            model.hideDialog(dialog);
          } else {
            model.hideOverlay(overlay);
          }
        }
      });
    }
  }

  /**
   * Makes the {@code overlay} <b>in</b>visible, along with its {@code glassPane}.
   *
   * @param overlay to be hidden
   */
  public void hideOverlay(Node overlay) {
    view.hideOverlay(overlay);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setupBindings() {

  }
}