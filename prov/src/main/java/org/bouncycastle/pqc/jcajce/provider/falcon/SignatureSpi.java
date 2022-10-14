package org.bouncycastle.pqc.jcajce.provider.falcon;

import java.io.ByteArrayOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.spec.AlgorithmParameterSpec;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.pqc.crypto.falcon.FalconParameters;
import org.bouncycastle.pqc.crypto.falcon.FalconPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.falcon.FalconSigner;

public class SignatureSpi
    extends java.security.Signature
{
    private ByteArrayOutputStream bOut;
    private FalconSigner signer;
    private SecureRandom random;
    private FalconParameters parameters;

    protected SignatureSpi(FalconSigner signer)
    {
        super("Falcon");
        
        this.bOut = new ByteArrayOutputStream();
        this.signer = signer;
        this.parameters = null;
    }

    protected SignatureSpi(FalconSigner signer, FalconParameters parameters)
    {
        super(parameters.getName());
        this.parameters = parameters;

        this.bOut = new ByteArrayOutputStream();
        this.signer = signer;
    }

    protected void engineInitVerify(PublicKey publicKey)
        throws InvalidKeyException
    {
        if (publicKey instanceof BCFalconPublicKey)
        {
            BCFalconPublicKey key = (BCFalconPublicKey)publicKey;
            CipherParameters param = key.getKeyParams();

            if (parameters != null)
            {
                if (!parameters.getName().equals(key.getAlgorithm()))
                {
                    throw new InvalidKeyException("signature configured for " + parameters.getName());
                }
            }

            signer.init(false, param);
        }
        else
        {
            throw new InvalidKeyException("unknown public key passed to Falcon");
        }
    }

    protected void engineInitSign(PrivateKey privateKey, SecureRandom random)
        throws InvalidKeyException
    {
        this.random = random;
        engineInitSign(privateKey);
    }

    protected void engineInitSign(PrivateKey privateKey)
        throws InvalidKeyException
    {
        if (privateKey instanceof BCFalconPrivateKey)
        {
            BCFalconPrivateKey key = (BCFalconPrivateKey)privateKey;
            CipherParameters param = key.getKeyParams();

            if (parameters != null)
            {
                if (!parameters.getName().equals(key.getAlgorithm()))
                {
                    throw new InvalidKeyException("signature configured for " + parameters.getName());
                }
            }

            if (random != null)
            {
                signer.init(true, new ParametersWithRandom(param, random));
            }
            else
            {
                signer.init(true, param);
            }
        }
        else
        {
            throw new InvalidKeyException("unknown private key passed to Falcon");
        }
    }

    protected void engineUpdate(byte b)
            throws SignatureException
    {
        bOut.write(b);
    }

    protected void engineUpdate(byte[] b, int off, int len)
            throws SignatureException
    {
        bOut.write(b, off, len);
    }

    protected byte[] engineSign()
        throws SignatureException
    {
        try
        {
            byte[] message = bOut.toByteArray();

            bOut.reset();

            return signer.generateSignature(message);
        }
        catch (Exception e)
        {
            throw new SignatureException(e.toString());
        }
    }

    protected boolean engineVerify(byte[] sigBytes)
        throws SignatureException
    {
        byte[] message = bOut.toByteArray();

        bOut.reset();

        return signer.verifySignature(message, sigBytes);
    }

    protected void engineSetParameter(AlgorithmParameterSpec params)
    {
        // TODO
        throw new UnsupportedOperationException("engineSetParameter unsupported");
    }

    /**
     * @deprecated replaced with #engineSetParameter(java.security.spec.AlgorithmParameterSpec)
     */
    protected void engineSetParameter(String param, Object value)
    {
        throw new UnsupportedOperationException("engineSetParameter unsupported");
    }

    /**
     * @deprecated
     */
    protected Object engineGetParameter(String param)
    {
        throw new UnsupportedOperationException("engineSetParameter unsupported");
    }

    public static class Base
        extends SignatureSpi
    {
        public Base()
        {
            super(new FalconSigner());
        }
    }

    public static class Falcon512
        extends SignatureSpi
    {
        public Falcon512()
        {
            super(new FalconSigner(), FalconParameters.falcon_512);
        }
    }

    public static class Falcon1024
        extends SignatureSpi
    {
        public Falcon1024()
        {
            super(new FalconSigner(), FalconParameters.falcon_1024);
        }
    }
}
