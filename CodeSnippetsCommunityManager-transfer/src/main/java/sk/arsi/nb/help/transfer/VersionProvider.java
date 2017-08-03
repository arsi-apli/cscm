/*
 * (C) Copyright 2017 Arsi (http://www.arsi.sk/).
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
 *
 */

package sk.arsi.nb.help.transfer;

import java.io.IOException;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.logging.Logger;

/**
 *
 * @author arsi
 */
public final class VersionProvider {

    private static final Logger LOGGER = Logger.getLogger(VersionProvider.class.getName());
    public static final String UNSUPPORTED_TRANSFER_VERSION = "UNSUPPORTED_TRANSFER_VERSION";

    public static final VersionProvider INSTANCE = new VersionProvider();
    private String version = "0.0";

    private VersionProvider() {
        try {
            PropertyResourceBundle bundle = new PropertyResourceBundle(this.getClass().getResourceAsStream("version.properties"));
            version = bundle.getString("transfer.version");
        } catch (MissingResourceException e) {
            LOGGER.warning("Resource bundle 'version.properties' was not found or error while reading current version.");
        } catch (IOException ex) {
        }
    }

    public String getVersion() {
        return INSTANCE.version;
    }
}
