/* Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */

package edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.core;

/**
 * Interface that those Jackson components that are explicitly versioned will implement.
 * Intention is to allow both plug-in components (custom extensions) and applications and
 * frameworks that use Jackson to detect exact version of Jackson in use.
 * This may be useful for example for ensuring that proper Jackson version is deployed
 * (beyond mechanisms that deployment system may have), as well as for possible
 * workarounds.
 */
public interface Versioned {
    /**
     * Method called to detect version of the component that implements this interface;
     * returned version should never be null, but may return specific "not available"
     * instance (see {@link Version} for details).
     *
     * @return Version of the component
     */
    Version version();
}
