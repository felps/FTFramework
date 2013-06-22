/**
 * 
 */
package asct.core.corba;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import arsc.ArscImpl;
import asct.shared.LoginCallbackHandler;
import clusterManagement.ApplicationNotFoundException;
import clusterManagement.ApplicationRegistrationException;
import clusterManagement.ApplicationRepository;
import clusterManagement.BinaryCreationException;
import clusterManagement.BinaryNotFoundException;
import clusterManagement.ContextInitiationException;
import clusterManagement.DirectoryCreationException;
import clusterManagement.DirectoryNotEmptyException;
import clusterManagement.DirectoryNotFoundException;
import clusterManagement.FileIOException;
import clusterManagement.InvalidPathNameException;
import clusterManagement.SecurityException;
import clusterManagement.SignatureCheckingException;
import clusterManagement.SignatureRequestException;
import dataTypes.ApplicationDescription;
import dataTypes.BinaryDescription;
import dataTypes.ContentDescription;

/**
 * @author Braga
 *
 */
public class SecureApplicationRepositoryWrapper extends
		ApplicationRepositoryStubWrapper {
    private ArscImpl arsc_=null;	
    
    public SecureApplicationRepositoryWrapper(ApplicationRepository applicationRepository, CallbackHandler handler) throws ContextInitiationException
    {
      super(applicationRepository);
      

      	arsc_ = new ArscImpl();
      	applicationRepository_ = applicationRepository;
		
//	    Complete name is DOMAIN + NAME
		NameCallback nameCallback= new NameCallback("Username");
		PasswordCallback passwordCallback=new PasswordCallback("Password",false);
		Callback[] callbacks = {nameCallback,passwordCallback};
		try {
			handler.handle(callbacks);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedCallbackException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String completeName = nameCallback.getName() + "/ARSC"; // Setting the complete name 
		String password = new String (passwordCallback.getPassword());
		LoginCallbackHandler completeNameHandler = new LoginCallbackHandler(completeName,password);
		completeName=null;password=null;handler=null; //destroying handler
		
		arsc_.initContext(completeNameHandler);
      
      
    }

	public ContentDescription[] listDirectoryContents(String directoryName)
			throws DirectoryNotFoundException, InvalidPathNameException, SecurityException {
		try {
			directoryName = arsc_.requestSignature(directoryName);
		} catch (SignatureRequestException e) {
			throw new SecurityException(e.toString());
		}
		ContentDescription[] returnContents = applicationRepository_.listDirectoryContents(directoryName);
    		for(int i =0 ; i < returnContents.length;i++)
		{
			try {
				returnContents[i].fileName = arsc_.checkSignature(returnContents[i].fileName);
			} catch (SignatureCheckingException e) {
				throw new SecurityException(e.toString());
			}
			//returnContents[i].kind = arsc_.checkSignature(returnContents[i].kind);
		}
		return returnContents;
	}

	public void createDirectory(String directoryName)
			throws DirectoryCreationException, InvalidPathNameException, SecurityException {
		try {
			directoryName=arsc_.requestSignature(directoryName);
		} catch (SignatureRequestException e) {
			throw new SecurityException(e.toString());
		} 
		applicationRepository_.createDirectory(directoryName);
	}

	public void removeDirectory(String directoryName)
			throws DirectoryNotFoundException, DirectoryNotEmptyException,
			InvalidPathNameException, SecurityException {
		try {
			directoryName=arsc_.requestSignature(directoryName);
		} catch (SignatureRequestException e) {
			throw new SecurityException(e.toString());
		}
		applicationRepository_.removeDirectory(directoryName);
	}

	public void registerApplication(String basePath, String applicationName)
			throws ApplicationRegistrationException,
			DirectoryCreationException, InvalidPathNameException, SecurityException {
		try{
			basePath=arsc_.requestSignature(basePath);
			applicationName=arsc_.requestSignature(applicationName);
		}catch(SignatureRequestException e)
		{
			throw new SecurityException(e.toString());
		}
		applicationRepository_.registerApplication(basePath, applicationName);

	}

	public void unregisterApplication(String basePath, String applicationName)
			throws ApplicationNotFoundException, DirectoryNotFoundException,
			DirectoryNotEmptyException, InvalidPathNameException, SecurityException {
		try {
			basePath=arsc_.requestSignature(basePath);
			applicationName=arsc_.requestSignature(applicationName);
		} catch (SignatureRequestException e) {
			throw new SecurityException(e.toString());
		}

		applicationRepository_.unregisterApplication(basePath, applicationName);

	}

	public void uploadApplicationBinary(BinaryDescription binaryDescription, byte[] binaryCode)
			throws BinaryCreationException, ApplicationNotFoundException,
			DirectoryNotFoundException, InvalidPathNameException, SecurityException {
		try{
			binaryCode = arsc_.requestSignature(binaryCode);
			binaryDescription.applicationName=arsc_.requestSignature(binaryDescription.applicationName);
			binaryDescription.basePath=arsc_.requestSignature(binaryDescription.basePath);
			binaryDescription.binaryName=arsc_.requestSignature(binaryDescription.binaryName);
			binaryDescription.description=arsc_.requestSignature(binaryDescription.description);
			} catch (SignatureRequestException e) {
				throw new SecurityException(e.toString());
			}
		applicationRepository_.uploadApplicationBinary(binaryDescription, binaryCode);
	}

	public void deleteApplicationBinary(String basePath, String applicationName, String binaryName) throws ApplicationNotFoundException,
			DirectoryNotFoundException, BinaryNotFoundException,
			InvalidPathNameException, SecurityException {
		try{
		basePath=arsc_.requestSignature(basePath);
		applicationName=arsc_.requestSignature(applicationName);
		binaryName=arsc_.requestSignature(binaryName);
		} catch (SignatureRequestException e) {
			throw new SecurityException(e.toString());
		}
		applicationRepository_.deleteApplicationBinary(basePath, applicationName, binaryName);
	}

	public ApplicationDescription getApplicationDescription(String basePath,
			String applicationName) throws ApplicationNotFoundException,
			DirectoryNotFoundException, InvalidPathNameException, SecurityException {
		try{
		basePath=arsc_.requestSignature(basePath);
		applicationName=arsc_.requestSignature(applicationName);
		} catch (SignatureRequestException e) {
			throw new SecurityException(e.toString());
		}
		ApplicationDescription returnApplicationDescription = applicationRepository_.getApplicationDescription(basePath, applicationName); 
		try {
			returnApplicationDescription.applicationName=arsc_.checkSignature(returnApplicationDescription.applicationName);
			returnApplicationDescription.basePath=arsc_.checkSignature(returnApplicationDescription.basePath);
		} catch (SignatureCheckingException e) {
			// TODO Auto-generated catch block
			throw new SecurityException(e.toString());
		}
		
			for(int i=0;i<returnApplicationDescription.binaryIds.length;i++)
			{
				try {
					returnApplicationDescription.binaryIds[i]=arsc_.checkSignature(returnApplicationDescription.binaryIds[i]);
					
			} catch (SignatureCheckingException e) {
				// TODO Auto-generated catch block
				throw new SecurityException(e.toString());
			}
			}
			try {
				returnApplicationDescription.numberOfBinaries=arsc_.checkSignature(returnApplicationDescription.numberOfBinaries);
			} catch (SignatureCheckingException e) {
				throw new SecurityException(e.toString());
			}
			return returnApplicationDescription;
	}

	public byte[] getApplicationBinary(String basePath, String applicationName,
			String binaryName) throws InvalidPathNameException, ApplicationNotFoundException, DirectoryNotFoundException, BinaryNotFoundException, FileIOException, SecurityException {
		try{
		basePath=arsc_.requestSignature(basePath);
		applicationName=arsc_.requestSignature(applicationName);
		binaryName=arsc_.requestSignature(binaryName);
			} catch (SignatureRequestException e) {
				// TODO Auto-generated catch block
				throw new SecurityException(e.toString());
			}
		byte[] returnApplicationBinary=	applicationRepository_.getApplicationBinary(basePath,applicationName,binaryName);
		try{
		returnApplicationBinary = arsc_.checkSignature(returnApplicationBinary);
		} catch (SignatureCheckingException e) {
			// TODO Auto-generated catch block
			throw new SecurityException(e.toString());
		}
		return returnApplicationBinary;
	}

	public byte[] getRemoteApplicationBinary(String basePath, 
				String applicationName, String binaryName, 
				String applicationRepositoryIor) throws InvalidPathNameException, ApplicationNotFoundException, DirectoryNotFoundException, BinaryNotFoundException, FileIOException, SecurityException {
		
		try{
		basePath=arsc_.requestSignature(basePath);
		applicationName=arsc_.requestSignature(applicationName);
		binaryName=arsc_.requestSignature(binaryName);
		applicationRepositoryIor=arsc_.requestSignature(applicationRepositoryIor);
		} catch (SignatureRequestException e) {
			// TODO Auto-generated catch block
			throw new SecurityException(e.toString());
		}
		byte[] returnApplicationRepository = applicationRepository_.getRemoteApplicationBinary(basePath, applicationName, binaryName,  applicationRepositoryIor);
		return returnApplicationRepository;
	}
	
}
