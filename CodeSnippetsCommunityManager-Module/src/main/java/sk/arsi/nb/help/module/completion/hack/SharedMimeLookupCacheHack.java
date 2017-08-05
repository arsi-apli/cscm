/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.arsi.nb.help.module.completion.hack;

import java.util.Collection;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.editor.mimelookup.APIAccessor;
import org.netbeans.modules.editor.mimelookup.MimeLookupCacheSPI;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.ServiceProvider;
import sk.arsi.nb.help.module.completion.HelpCompletionProvider;

/**
 *
 * @author arsi
 */
@ServiceProvider(service = MimeLookupCacheSPI.class, position = 0x7ffffffe)
public class SharedMimeLookupCacheHack extends MimeLookupCacheSPI {

    private static final Lookup completionProviderLookup = Lookups.singleton(new HelpCompletionProvider());

    @Override
    public synchronized Lookup getLookup(MimePath mp) {
        Collection<? extends MimeLookupCacheSPI> providers = Lookup.getDefault().lookupAll(MimeLookupCacheSPI.class);
        for (MimeLookupCacheSPI next : providers) {
            if (!next.equals(this)) {
                return new ProxyLookup(next.getLookup(mp), completionProviderLookup);
            }
        }
        return new ProxyLookup(APIAccessor.get().cacheMimeLookup(mp), completionProviderLookup);
    }
}
