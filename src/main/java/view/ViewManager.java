package view;

import java.awt.CardLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import interface_adapter.ViewManagerModel;

public class ViewManager extends JPanel implements PropertyChangeListener {

    private final CardLayout cardLayout = new CardLayout();
    private final ViewManagerModel viewManagerModel;

    public ViewManager(final ViewManagerModel viewManagerModel) {
        this.viewManagerModel = viewManagerModel;
        setLayout(cardLayout);
        viewManagerModel.addPropertyChangeListener(this);
    }

    public void registerView(final String viewName, final JPanel view) {
        add(view, viewName);
        cardLayout.show(this, viewManagerModel.getActiveView());
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        if ("activeView".equals(event.getPropertyName())) {
            final Runnable showView = () -> {
                cardLayout.show(this, viewManagerModel.getActiveView());
                revalidate();
                repaint();
            };

            if (SwingUtilities.isEventDispatchThread()) {
                showView.run();
            }
            else {
                SwingUtilities.invokeLater(showView);
            }
        }
    }
}
