package com.genability.client.api.service;

import static org.junit.Assert.*;

import com.genability.client.types.Account;
import com.genability.client.types.ClipBy;
import com.genability.client.types.GroupBy;
import com.genability.client.types.Response;
import com.genability.client.types.Profile;
import com.genability.client.api.request.GetProfileRequest;
import com.genability.client.api.request.GetProfilesRequest;
import com.genability.client.api.service.ProfileService;

import org.junit.Test;

public class ProfileServiceTests extends BaseServiceTests {

	private static ProfileService profileService = genabilityClient.getProfileService();
	
	
	@Test
	public void testGetProfile() {
		
		Profile newProfile = createProfile();
		GetProfileRequest request = new GetProfileRequest();
		
		// set a valid usageProfileId
		request.setProfileId(newProfile.getProfileId());
		request.setGroupBy(GroupBy.MONTH);
		request.setClipBy(ClipBy.OUTER);
		request.setPopulateReadings(true);
		callGetProfile("Test get one profile",request);
		
		cleanup(newProfile.getAccountId());
	}
	
	@Test
	public void testGetProfiles() {
		
		Profile newProfile = createProfile();
		GetProfilesRequest request = new GetProfilesRequest();
		request.setAccountId(newProfile.getAccountId());
		callGetProfiles("Test get all profiles",request);
		
		cleanup(newProfile.getAccountId());
	}
	
	@Test
	public void testAddProfile() {
		
		Account account = createAccount();
		Profile profile = new Profile();
		profile.setAccountId(account.getAccountId());
		Response<Profile> newProfile = profileService.addProfile(profile);
		
		assertNotNull("new Profile is null",newProfile);
		assertEquals("bad status",newProfile.getStatus(),Response.STATUS_SUCCESS);
		assertEquals("bad type",newProfile.getType(),Profile.REST_TYPE);
		assertTrue("bad count",newProfile.getCount() > 0);
		
		cleanup(account.getAccountId());
	}

	public void callGetProfile(String testCase, GetProfileRequest request) {
		
		Response<Profile> restResponse = profileService.getProfile(request);
		
		assertNotNull("restResponse null",restResponse);
		assertEquals("bad status",restResponse.getStatus(),Response.STATUS_SUCCESS);
		assertEquals("bad type",restResponse.getType(),Profile.REST_TYPE);
				
	}
	
	public void callGetProfiles(String testCase, GetProfilesRequest request) {
		
		Response<Profile> restResponse = profileService.getProfiles(request);
		
		assertNotNull("restResponse null",restResponse);
		assertEquals("bad status",restResponse.getStatus(),Response.STATUS_SUCCESS);
		assertEquals("bad type",restResponse.getType(),Profile.REST_TYPE);
				
	}

}