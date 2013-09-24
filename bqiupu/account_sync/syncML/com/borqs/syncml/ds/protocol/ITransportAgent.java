package com.borqs.syncml.ds.protocol;

import java.io.IOException;

import com.borqs.syncml.ds.exception.DsException;




public interface ITransportAgent {
	IResponse post(IRequest request) throws DsException, IOException;

	void shutDown();
}
