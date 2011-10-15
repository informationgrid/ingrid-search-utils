/**
 * 
 */
package de.ingrid.search.utils;

import org.apache.log4j.Logger;

import de.ingrid.utils.IConfigurable;
import de.ingrid.utils.PlugDescription;

/**
 * Simple Plugdescription wrapper that is configurable. It gets the current
 * plugdescription during the configure phase. It is used to wrap the
 * plugdescription so it can be injected into the facet search.
 * 
 * @author joachim@wemove.com
 * 
 */
public class ConfigurablePlugDescriptionWrapper implements IConfigurable {

    PlugDescription plugDescription = null;

    private static Logger LOG = Logger.getLogger(ConfigurablePlugDescriptionWrapper.class);

    public ConfigurablePlugDescriptionWrapper() {

    }

    public ConfigurablePlugDescriptionWrapper(PlugDescription plugDescription) {
        this.plugDescription = plugDescription;
        if (LOG.isInfoEnabled()) {
            LOG.info("Configure called, update plugdescription.");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.ingrid.utils.IConfigurable#configure(de.ingrid.utils.PlugDescription)
     */
    @Override
    public void configure(PlugDescription plugDescription) {
        this.plugDescription = plugDescription;
    }

    public PlugDescription getPlugDescription() {
        return plugDescription;
    }

    public void setPlugDescription(PlugDescription plugDescription) {
        this.plugDescription = plugDescription;
    }

}
