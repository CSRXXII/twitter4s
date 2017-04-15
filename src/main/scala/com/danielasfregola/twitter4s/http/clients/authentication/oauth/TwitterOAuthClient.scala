package com.danielasfregola.twitter4s.http
package clients.authentication.oauth

import com.danielasfregola.twitter4s.entities.authentication.OAuthToken
import com.danielasfregola.twitter4s.entities.enums.AccessType.AccessType
import com.danielasfregola.twitter4s.http.clients.authentication.AuthenticationClient
import com.danielasfregola.twitter4s.http.clients.authentication.oauth.parameters.RequestTokenParameters
import com.danielasfregola.twitter4s.util.Configurations._

import scala.concurrent.Future

/** Implements the available requests for the `oauth` resource.
  * */
trait TwitterOAuthClient {

  protected val authenticationClient: AuthenticationClient

  private val oauthUrl = s"$apiTwitterUrl/oauth"

  /** Allows a Consumer application to obtain an OAuth Request Token to request user authorization.
    * This method fulfills <a href="http://oauth.net/core/1.0/#auth_step1">Section 6.1</a> of the
    * <a href="http://oauth.net/core/1.0/#anchor9">OAuth 1.0 authentication flow</a>.
    * For more information see
    * <a href="https://dev.twitter.com/oauth/reference/post/oauth/request_token" target="_blank">
    *   https://dev.twitter.com/oauth/reference/post/oauth/request_token</a>.
    *
    * @param oauth_callback : Optional, by default is `None`.
    *                         For OAuth 1.0a compliance this parameter is required.
    *                         The value you specify here will be used as the URL a user is redirected
    *                         to should they approve your application’s access to their account.
    *                         Set this to oob for out-of-band pin mode. This is also how you specify
    *                         custom callbacks for use in desktop/mobile applications.
    * @param x_auth_access_type : Optional, by default is `None`.
    *                             Overrides the access level an application requests to a users account.
    *                             Supported values are read or write . This parameter is intended to
    *                             allow a developer to register a read/write application but also request
    *                             read only access when appropriate.
    * @return : The authentication token.
    * */
  def requestToken(oauth_callback: Option[String] = None, x_auth_access_type: Option[AccessType] = None): Future[OAuthToken] = {
    import authenticationClient._
    val parameters = RequestTokenParameters(x_auth_access_type)
    Post(s"$oauthUrl/request_token", parameters).respondAs[OAuthToken](oauth_callback)
  }

  /** Allows a Consumer application to use an OAuth request_token to request user authorization.
    * This method is a replacement of <a href="http://oauth.net/core/1.0/#auth_step2">Section 6.2</a>
    * of the <a href="http://oauth.net/core/1.0/#anchor9">OAuth 1.0 authentication flow</a>
    * for applications using the callback authentication flow.
    * The method will use the currently logged in user as the account for access authorization unless
    * the `force_login` parameter is set to true.
    * This method differs from [[authorizeUrl]] in that if the user has already granted the application permission,
    * the redirect will occur without the user having to re-approve the application.
    * To realize this behavior, you must enable the `Use Sign in with Twitter` setting on your
    * <a href="https://apps.twitter.com/">application record</a>.
    * For more information see
    * <a href="https://dev.twitter.com/oauth/reference/get/oauth/authenticate" target="_blank">
    *   https://dev.twitter.com/oauth/reference/get/oauth/authenticate</a>.
    *
    * @param oauth_token : The `OAuthToken.oauth_token` obtained from [[requestToken]]
    * @param force_login : By default is `false`. When set to `true`, it forces the user to
    *                      enter their credentials to ensure the correct users account is authorized.
    * @param screen_name : Optional, by default it is `None`.
    *                      It prefills the username input box of the OAuth login screen with the given value.
    * @return : The authentication url to use in a web browser for the user to complete the authentication process.
    * */
  def authenticateUrl(oauth_token: String, force_login: Boolean = false, screen_name: Option[String] = None): String =
    genericOAuthUrl("authenticate")(oauth_token, force_login, screen_name)

  /** Allows a Consumer application to use an OAuth Request Token to request user authorization.
    * This method fulfills <a href="http://oauth.net/core/1.0/#auth_step2">Section 6.2</a>
    * of the <a href="http://oauth.net/core/1.0/#anchor9">OAuth 1.0 authentication flow</a>.
    * Desktop applications must use this method (and cannot use [[authenticateUrl]]).
    * For more information see
    * <a href="https://dev.twitter.com/oauth/reference/get/oauth/authenticate" target="_blank">
    *   https://dev.twitter.com/oauth/reference/get/oauth/authenticate</a>.
    *
    * @param oauth_token : The `OAuthToken.oauth_token` obtained from [[requestToken]]
    * @param force_login : By default is `false`. When set to `true`, it forces the user to
    *                      enter their credentials to ensure the correct users account is authorized.
    * @param screen_name : Optional, by default it is `None`.
    *                      It prefills the username input box of the OAuth login screen with the given value.
    * @return : The url to use in a web browser for the user to complete the authorization process.
    * */
  def authorizeUrl(oauth_token: String, force_login: Boolean = false, screen_name: Option[String] = None): String =
    genericOAuthUrl("authorize")(oauth_token, force_login, screen_name)

  private def genericOAuthUrl(path: String)(oauth_token: String, force_login: Boolean = false, screen_name: Option[String] = None): String = {
    val params = {
      val queryParams = List(Some("oauth_token" -> oauth_token),
        Some("force_login" -> force_login),
        screen_name.map(n => "screen_name" -> n))
      queryParams.flatten.map { case (key, value) => s"$key=$value"}.mkString("&")
    }
    s"$oauthUrl/$path?$params"
  }
}
