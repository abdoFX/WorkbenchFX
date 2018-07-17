package com.dlsc.workbenchfx.custom.customer;

import com.dlsc.workbenchfx.model.WorkbenchModule;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.scene.Node;

public class CustomerModule extends WorkbenchModule {

  public CustomerModule() {
    super("Customer Management Customer ManagementCustomer ManagementCustomer ManagementCustomer Management", FontAwesomeIcon.USERS);
  }

  @Override
  public Node activate() {
    return new CustomerView();
  }

}
