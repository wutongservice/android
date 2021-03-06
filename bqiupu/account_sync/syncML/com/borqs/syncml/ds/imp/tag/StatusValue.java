package com.borqs.syncml.ds.imp.tag;

public class StatusValue {

	// Informational 1xx
	public static final int IN_PROGRESS = 101;//	
	// In progress. The specified SyncML command is being carried
	// out, but has not yet completed.
	// Successful 2xx ;//
	public static final int SUCCESS = 200;//	
	// OK. The SyncML command completed successfully.
	public static final int ITEM_ADDED = 201;//	
	// Item added. The requested item was added.
	public static final int ACCEPTED_FOR_PROCESSING = 202;//	
	// Accepted for processing. The request to either run a remote
	// execution of an application or to alert a user or application
	// was successfully performed.
	public static final int NON_AUTHORITATIVE_RESPONSE = 203;//	
	// Non-authoritative response. The request is being responded
	// to by an entity other than the one targeted. The response is
	// only to be returned when the request would have been
	// resulted in a 200 response code from the authoritative target.
	public static final int NO_CONTENT = 204;//	
	// No content. The request was successfully completed but no
	// data is being returned. The response code is also returned in
	public static final int RESET_CONTENT = // response to a Get when the target
	// has no content.
	205;//	
	// Reset content. The source should update their content. The
	// originator of the request is being told that their content should
	// be synchronized to get an up to date version.
	public static final int PARTIAL_CONTENT = 206;//	
	// Partial content. The response indicates that only part of the
	// command was completed. If the remainder of the command
	// can be completed later, then when completed another
	// appropriate completion request status code SHOULD be
	// created.
	public static final int CONFLICT_RESOLVED_WITH_MERGE = 207;//	
	// Conflict resolved with merge. The response indicates that the
	// request created a conflict; which was resolved with a merge
	// of the client and server instances of the data. The response
	// includes both the Target and Source URLs in the Item of the
	// Status. In addition, a Replace command is returned with
	// the merged data.
	public static final int CONFLICT_CLIENT_WINNING = 208;//	
	// Conflict resolved with client's command "winning". The
	// response indicates that there was an update conflict; which
	// was resolved by the client command winning.
	public static final int CONFLICT_RESOLVED_WITH_DUPLICATE = 209;//	
	// Conflict resolved with duplicate. The response indicates that
	// the request created an update conflict; which was resolved
	// with a duplication of the client's data being created in the
	// server database. The response includes both the target URI
	// of the duplicate in the Item of the Status. In addition, in the
	// case of a two-way synchronization, an Add command is
	// returned with the duplicate data definition.
	public static final int DELETE_WITHOUT_ARCHIVE = 210;//	
	// Delete without archive. The response indicates that the
	// requested data was successfully deleted, but that it was not
	// archived prior to deletion because this optional feature was
	// not supported by the implementation.
	public static final int ITEM_NOT_DELETED = 211;//	
	// Item not deleted. The requested item was not found. It may
	// have been previously deleted.
	public static final int AUTHENTICATION_ACCEPTED = 212;//	
	// Authentication accepted. No further authentication is needed
	// for the remainder of the synchronization session. This
	// response code can only be used in response to a request in
	// which the credentials were provided.
	public static final int CHUNKED_ITEM_ACCEPTED_AND_BUFFERED = 213;//	
	// Chunked item accepted and buffered.
	public static final int OPERATION_CANCELLED = 214;//	
	// Operation cancelled. The SyncML command completed
	// successfully, but no more commands will be processed
	// within the session.
	public static final int NOT_EXECUTED = 215;//	
	// Not executed. A command was not executed, as a result of
	// user interaction and user chose not to accept the choice.
	public static final int ATOMIC_ROLL_BACK_OK = 216;//	
	// Atomic roll back OK. A command was inside Atomic
	// element and Atomic failed. This command was rolled back
	// successfully.
	// Redirection 3xx ;//
	public static final int MULTIPLE_CHOICES = 300;//	
	// Multiple choices. The requested target is one of a number of
	// multiple alternatives requested target. The alternative
	// SHOULD also be returned in the Item element type in the
	// Status.
	public static final int MOVED_PERMANENTLY = 301;//	
	// Moved permanently. The requested target has a new URI.
	// The new URI SHOULD also be returned in the Item element
	// type in the Status.
	public static final int FOUND = 302;//	
	// Found. The requested target has temporarily moved to a
	// different URI. The original URI SHOULD continue to be used.
	// The URI of the temporary location SHOULD also be returned
	// in the Item element type in the Status. The requestor
	// SHOULD confirm the identity and authority of the temporary
	// URI to act on behalf of the original target URI.
	public static final int SEE_OTHER = 303;//	
	// See other. The requested target can be found at another
	// URI. The other URI SHOULD be returned in the Item
	// element type in the Status.
	public static final int NOT_MODIFIED = 304;//	
	// Not modified. The requested SyncML command was not
	// executed on the target. This is an additional response that
	// can be added to any of the other Redirection response
	// codes.
	public static final int USE_PROXY = 305;//	
	// Use proxy. The requested target MUST be accessed through
	// the specified proxy URI. The proxy URI SHOULD also be
	// returned in the Item element type in the Status.
	// Originator Exceptions 4xx ;//
	public static final int BAD_REQUEST = 400;//	
	// Bad request. The requested command could not be
	// performed because of malformed syntax in the command.
	// The malformed command MAY also be returned in the Item
	// element type in the Status.
	public static final int INVALID_CREDENTIALS = 401;//	
	// Invalid credentials. The requested command failed because
	// the requestor MUST provide proper authentication. If the
	// property type of authentication was presented in the original
	// request, then the response code indicates that the requested
	// command has been refused for those credentials.
	public static final int PAYMENT_REQUIRED = 402;//	
	// Payment required. The requested command failed because
	// proper payment is required. This version of SyncML does not
	// standardize the payment mechanism.
	public static final int FORBIDDEN = 403;//	
	// Forbidden. The requested command failed, but the recipient
	// understood the requested command. Authentication will not
	// help and the request SHOULD NOT be repeated. If the
	// recipient wishes to make public why the request was denied,
	// then a description MAY be specified in the Item element
	// type in the Status. If the recipient does not wish to make
	// public why the request was denied then the response code
	// 404 MAY be used instead.
	public static final int NOT_FOUND = 404;//	
	// Not found. The requested target was not found. No indication
	// is given as to whether this is a temporary or permanent
	// condition. The response code 410 SHOULD be used when
	// the condition is permanent and the recipient wishes to make
	// this fact public. This response code is also used when the
	// recipient does not want to make public the reason for why a
	// requested command is not allowed or when no other
	// response code is appropriate.
	public static final int COMMAND_NOT_ALLOWED = 405;//	
	// Command not allowed. The requested command is not
	// allowed on the target. The recipient SHOULD return the
	// allowed command for the target in the Item element type in
	// the Status.
	public static final int OPTIONAL_FEATURE_NOT_SUPPORTED = 406;//	
	// Optional feature not supported. The requested command
	// failed because an optional feature in the request was not
	// supported. The unsupported feature SHOULD be specified
	// by the Item element type in the Status.
	public static final int MISSING_CREDENTIALS = 407;//	
	// Missing credentials. This response code is similar to 401
	// except that the response code indicates that the originator
	// MUST first authenticate with the recipient. The recipient
	// SHOULD also return the suitable challenge in the Chal
	// element type in the Status.
	public static final int REQUEST_TIMEOUT = 408;//	
	// Request timeout. An expected message was not received
	// within the required period of time. The request can be
	// repeated at another time. The RespURI can be used to
	// specify the URI and optionally the date/time after which the
	// originator can repeat the request. See RespURI for details.
	public static final int CONFLICT = 409;//	
	// Conflict. The requested failed because of an update conflict
	// between the client and server versions of the data. Setting of
	// the conflict resolution policy is outside the scope of this
	// version of SyncML. However, identification of conflict
	// resolution performed, if any, is within the scope.
	public static final int GONE = 410;//	
	// Gone. The requested target is no longer on the recipient and
	// no forwarding URI is known.
	public static final int SIZE_REQUIRED = 411;//	
	// Size required. The requested command MUST be
	// accompanied by byte size or length information in the Meta
	// element type.
	public static final int INCOMPLETE_COMMAND = 412;//	
	// Incomplete command. The requested command failed on the
	// recipient because it was incomplete or incorrectly formed.
	// The recipient SHOULD specify the portion of the command
	// that was incomplete or incorrect in the Item element type in
	// the Status.
	public static final int REQUEST_ENTITY_TOO_LARGE = 413;//	
	// Request entity too large. The recipient is refusing to perform
	// the requested command because the requested item is
	// larger than the recipient is able or willing to process. If the
	// condition is temporary, the recipient SHOULD also include a
	// Status with the response code 418 and specify a RespURI
	// with the response URI and optionally the date/time that the
	// command SHOULD be repeated.
	public static final int URI_TOO_LONG = 414;//	
	// URI too long. The requested command failed because the
	// target URI is too long for what the recipient is able or willing
	// to process. This response code is seldom encountered, but is
	// used when a recipient perceives that an intruder may be
	// attempting to exploit security holes or other defects in order
	// to threaten the recipient.
	public static final int UNSUPPORTED_MEDIA_TYPE_OR_FORMAT = 415;//	
	// Unsupported media type or format. The unsupported content
	// type or format SHOULD also be identified in the Item
	// element type in the Status.
	public static final int REQUESTED_SIZE_TOO_BIG = 416;//	
	// Requested size too big. The request failed because the
	// specified byte size in the request was too big.
	public static final int RETRY_LATER = 417;//	
	// Retry later. The request failed at this time and the originator
	// should retry the request later. The recipient SHOULD specify
	// a RespURI with the response URI and the date/time that the
	// command SHOULD be repeated.
	public static final int ALREADY_EXISTS = 418;//	
	// Already exists. The requested Put or Add command failed
	// because the target already exists.
	public static final int CONFLICT_RESOLVED_WITH_SERVER_DATA = 419;//	
	// Conflict resolved with server data. The response indicates
	// that the client request created a conflict; which was resolved
	// by the server command winning. The normal information in
	// the Status should be sufficient for the client to "undo" the
	// resolution, if it is desired.
	public static final int DEVICE_FULL = 420;//	
	// Device full. The response indicates that the recipient has no
	// more storage space for the remaining synchronization data.
	// The response includes the remaining number of data that
	// could not be returned to the originator in the Item of the
	// Status.
	public static final int UNKNOWN_SEARCH_GRAMMAR = 421;//	
	// Unknown search grammar. The requested command failed
	// on the server because the specified search grammar was not
	// known. The client SHOULD re-specify the search using a
	// known search grammar.
	public static final int BAD_CGI_SCRIPT = 422;//	
	// Bad CGI Script. The requested command failed on the server
	// because the CGI scripting in the LocURI was incorrectly
	// formed. The client SHOULD re-specify the portion of the
	// command that was incorrect in the Item element type in the
	// Status.
	public static final int SOFT_DELETE_CONFLICT = 423;//	
	// Soft-delete conflict. The requested command failed because
	// the "Soft Deleted" item was previously "Hard Deleted" on the
	// server.
	public static final int SIZE_MISMATCH = 424;//	
	// Size mismatch. The chunked object was received, but the
	// size of the received object did not match the size declared
	// within the first chunk.
	// Recipient Exception 5xx ;//
	public static final int COMMAND_FAILED = 500;//	
	// Command failed. The recipient encountered an unexpected
	// condition which prevented it from fulfilling the request
	public static final int COMMAND_NOT_IMPLEMENTED = 501;//	
	// Command not implemented. The recipient does not support
	// the command required to fulfill the request. This is the
	// appropriate response when the recipient does not recognize
	// the requested command and is not capable of supporting it
	// for any resource.
	public static final int BAD_GATEWAY = 502;//	
	// Bad gateway. The recipient, while acting as a gateway or
	// proxy, received an invalid response from the upstream
	// recipient it accessed in attempting to fulfill the request.
	public static final int SERVICE_UNAVAILABLE = 503;//	
	// Service unavailable. The recipient is currently unable to
	// handle the request due to a temporary overloading or
	// maintenance of the recipient. The implication is that this is a
	// temporary condition; which will be alleviated after some
	// delay. The recipient SHOULD specify the URI and date/time
	// after which the originator should retry in the RespURI in the
	// response.
	public static final int GATEWAY_TIMEOUT = 504;//	
	// Gateway timeout. The recipient, while acting as a gateway or
	// proxy, did not receive a timely response from the upstream
	// recipient specified by the URI (e.g. HTTP, FTP, LDAP) or
	// some other auxiliary recipient (e.g. DNS) it needed to access
	// in attempting to complete the request.
	public static final int DTD_VERSION_NOT_SUPPORTED = 505;//	
	// DTD Version not supported. The recipient does not support
	// or refuses to support the specified version of SyncML DTD
	// used in the request SyncML Message. The recipient MUST
	// include the versions it does support in the Item element type
	// in the Status.
	public static final int PROCESSING_ERROR = 506;//	
	// Processing error. An application error occurred while
	// processing the request. The originator should retry the
	// request. The RespURI can contain the URI and date/time
	// after which the originator can retry the request.
	public static final int ATOMIC_FAILED = 507;//	
	// Atomic failed. The error caused all SyncML commands
	// within an Atomic element type to fail.
	public static final int REFRESH_REQUIRED = 508;//	
	// Refresh required. An error occurred that necessitates a
	// refresh of the current synchronization state of the client with
	// the server. Client is requested to initiate a slow sync with the
	// server.
	public static final int RESERVED = 509;//	
	// Reserved for future use.
	public static final int DATA_STORE_FAILURE = 510;//	
	// Data store failure. An error occurred while processing the
	// request. The error is related to a failure in the recipient data
	// store.
	public static final int SERVER_FAILURE = 511;//	
	// Server failure. A severe error occurred in the server while
	// processing the request. The originator SHOULD NOT retry
	// the request.
	public static final int SYNCHRONIZATION_FAILED = 512;//	
	// Synchronization failed. An application error occurred during
	// the synchronization session. The originator should restart the
	// synchronization session from the beginning.
	public static final int PROTOCOL_VERSION_NOT_SUPPORTED = 513;//	
	// Protocol Version not supported. The recipient does not
	// support or refuses to support the specified version of the
	// SyncML Synchronization Protocol used in the request
	// SyncML Message. The recipient MUST include the versions
	// it does support in the Item element type in the Status.
	public static final int OPERATION_CANCELLED_FAILED = 514;//	
	// Operation cancelled. The SyncML command was not
	// completed successfully, since the operation was already
	// cancelled before processing the command. The originator
	// should repeat the command in the next session.
	public static final int ATOMIC_ROLL_BACK_FAILED = 516;//	
	// Atomic roll back failed. Command was inside Atomic
	// element and Atomic failed. This command was not rolled
	// back successfully. Server should take action to try to recover
	// client back into original state.
	public static boolean isSuccess(int status) {
		return (status >= 200 && status < 300 
				|| status == CONFLICT 
				|| status == ALREADY_EXISTS
				|| status == CONFLICT_RESOLVED_WITH_SERVER_DATA
				|| status == SOFT_DELETE_CONFLICT
				);
	}

}
