package com.nuvei.backoffice.jalo;

import com.nuvei.backoffice.constants.NuveibackofficeConstants;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ExtensionManager;
import org.apache.log4j.Logger;

public class NuveibackofficeManager extends GeneratedNuveibackofficeManager
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger( NuveibackofficeManager.class.getName() );
	
	public static final NuveibackofficeManager getInstance()
	{
		ExtensionManager em = JaloSession.getCurrentSession().getExtensionManager();
		return (NuveibackofficeManager) em.getExtension(NuveibackofficeConstants.EXTENSIONNAME);
	}
	
}
