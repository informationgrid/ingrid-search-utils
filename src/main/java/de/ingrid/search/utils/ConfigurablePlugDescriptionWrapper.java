/*
 * **************************************************-
 * ingrid-search-utils
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
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
