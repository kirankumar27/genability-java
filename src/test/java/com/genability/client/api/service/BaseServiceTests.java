package com.genability.client.api.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genability.client.api.GenabilityClient;
import com.genability.client.api.request.DeleteAccountRequest;
import com.genability.client.types.Account;
import com.genability.client.types.Profile;
import com.genability.client.types.PropertyData;
import com.genability.client.types.ReadingData;
import com.genability.client.types.Response;
import com.genability.client.types.Tariff;

public class BaseServiceTests {

	protected Logger log = LoggerFactory.getLogger(this.getClass());
	
	protected static final GenabilityClient genabilityClient;
	protected static final AccountService accountService;
	protected static final ProfileService profileService;
	protected static final PropertyService propertyService;
	protected static final CalendarService calendarService;
	static {
		//
		// Very simple configuration of logging to console.
		//
		Logger logger = LoggerFactory.getLogger(BaseServiceTests.class);
		
		//
		// Very simple configuration of API keys etc.
		//
		Properties prop = new Properties();
		 
    	try {
    		//
    		//load the properties file from in the classpath
    		//
    		InputStream inputStream = BaseServiceTests.class.getClassLoader().getResourceAsStream("genability.properties");
                if (inputStream == null) {
                        logger.error("Can't find genability.properties");
                        throw new RuntimeException("Can't find genability.properties");
                }
    		prop.load(inputStream);
    	} catch (IOException ex) {
    		logger.error("Unable to process genability.properties", ex);
    		throw new RuntimeException(ex);
        }
 
  		//
        // get the properties and print them out
  		//
  		String appId = prop.getProperty("appId");
  		String appKey = prop.getProperty("appKey");
  		String restApiServer = prop.getProperty("restApiServer");
  		logger.info("appId: " + appId);
  		logger.info("appKey: " + appKey);
  		logger.info("restApiServer: " + restApiServer);
                if (appId == null || appId.trim().isEmpty() || appKey == null || appKey.trim().isEmpty()) {
                        logger.error("appId and appKey must be set");
                        throw new RuntimeException("Found one or more unset/empty properties");
                }

  		genabilityClient = new GenabilityClient(appId, appKey);
  		if (restApiServer != null && !restApiServer.equals("")) {
		    genabilityClient.setRestApiServer(restApiServer);
  		}
  		accountService = genabilityClient.getAccountService();
  		profileService = genabilityClient.getProfileService();
  		propertyService = genabilityClient.getPropertyService();
  		calendarService = genabilityClient.getCalendarService();
	}
	
	// Helper method:  We create an account and specify a tariff as well as values
	// for the tariff's properties.  We use masterTariffId 521 (PGE E-1) which for
	// calculations has one required property which is the territoryId.  
	// We also set the zipCode as an additional example.
	protected Account createAccount() {
		
		Account addAccount = new Account();
		addAccount.setAccountName("Java Client Lib Test Account - CAN DELETE");
		addAccount.setProviderAccountId("TEST-" + UUID.randomUUID());
		
		Map<String, PropertyData> properties = new HashMap<String, PropertyData>();
		
		PropertyData zipCodeData = new PropertyData();
		zipCodeData.setKeyName("zipCode");
		zipCodeData.setDataValue("94115");
		properties.put(zipCodeData.getKeyName(), zipCodeData);

		PropertyData territoryData = new PropertyData();
		territoryData.setKeyName("territoryId");
		territoryData.setDataValue("3538");
		properties.put(territoryData.getKeyName(), territoryData);

		addAccount.setProperties(properties);
		
		Tariff tariff = new Tariff();
		tariff.setMasterTariffId(521l);
		DateTime effDate = new DateTime(2012, 2, 1, 1, 0, 0, 0,DateTimeZone.forID("US/Pacific"));
		tariff.setEffectiveDate(effDate.toString());
		List<Tariff> tariffs = new ArrayList<Tariff>();
		tariffs.add(tariff);
		addAccount.setTariffs(tariffs);
							
		Response<Account> restResponse = accountService.addAccount(addAccount);
		
		assertNotNull("new account response is null",restResponse);
		assertEquals("bad status",restResponse.getStatus(),Response.STATUS_SUCCESS);
		assertEquals("bad type",restResponse.getType(),Account.REST_TYPE);
		assertTrue("bad count",restResponse.getCount() > 0);
		
		Account newAccount = null;
		for(Account account : restResponse.getResults()) {
			newAccount = account;
			assertNotNull("accountId null",account.getAccountId());
		}
		
		return newAccount;
	}
	
	protected Profile createProfile() {
		
		Account account = createAccount();
		Profile addProfile = new Profile();
		addProfile.setAccountId(account.getAccountId());
		addProfile.setProviderProfileId("TEST-" + UUID.randomUUID());
		Response<Profile> restResponse = profileService.addProfile(addProfile);
		
		assertNotNull("new Profile response is null",restResponse);
		assertEquals("bad status",restResponse.getStatus(),Response.STATUS_SUCCESS);
		assertEquals("bad type",restResponse.getType(),Profile.REST_TYPE);
		assertTrue("bad count",restResponse.getCount() > 0);
		
		Profile newProfile = null;
		for(Profile profile: restResponse.getResults()) {
			newProfile = profile;
			assertNotNull("profileId null", profile.getProfileId());
		}
		return newProfile;
	}

	protected Profile createProfileWithReadings(List<ReadingData> readings) {

		Account account = createAccount();
		Profile addProfile = new Profile();
		addProfile.setAccountId(account.getAccountId());
		addProfile.setReadingData(readings);

		Response<Profile> restResponse = profileService.addProfile(addProfile);

		assertNotNull("new Profile response is null", restResponse);
		assertEquals("bad status", restResponse.getStatus(),
				Response.STATUS_SUCCESS);
		assertEquals("bad type", restResponse.getType(), Profile.REST_TYPE);
		assertTrue("bad count", restResponse.getCount() > 0);

		Profile newProfile = null;
		for (Profile profile : restResponse.getResults()) {
			newProfile = profile;
			assertNotNull("profileId null", profile.getProfileId());
		}
		return newProfile;
	}

	// Delete the accounts we create to keep things clean; this also cleans up profiles
	// on the account
	protected void cleanup(String accountId) {
		DeleteAccountRequest deleteAccountRequest = new DeleteAccountRequest();
		deleteAccountRequest.setHardDelete(Boolean.TRUE);
		deleteAccountRequest.setAccountId(accountId);
		Response<Account> deleteResponse = accountService.deleteAccount(deleteAccountRequest);
		assertEquals("bad status",deleteResponse.getStatus(),Response.STATUS_SUCCESS);
	}
}
