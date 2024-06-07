package cbnu.subscribe_service.subscription.service.web;

public interface VerifyUserAccessRightsUseCase {

    VerifyUserAccessRightsResponse verify(VerifyUserAccessRightsCommand verifyUserAccessRightsCommand);
}
