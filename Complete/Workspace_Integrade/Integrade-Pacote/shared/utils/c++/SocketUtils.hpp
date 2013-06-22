// SocketUtils.cpp

#ifndef SOCKET_UTILS_HPP
#define SOCKET_UTILS_HPP

#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>

#include <iostream>

class SocketUtils {
public:

    //---------------------------------------------------------------------------
    static void writeUint32(int sockfd, long data) {

        uint32_t dataNl = htonl( (uint32_t)data );
        if (write(sockfd, &dataNl, sizeof(uint32_t)) <= 0) 
        std::cerr << "SocketUtils::writeUint32 --> " 
		          << "error while writing to stream (" << data << ")." << std::endl;
    
    }

    //---------------------------------------------------------------------------
    static long readUint32(int sockfd) {

        uint32_t dataSizeNl;
        if (read(sockfd, &dataSizeNl, sizeof(uint32_t)) < (signed)sizeof(uint32_t)) 
        std::cerr << "SocketUtils::readUint32 --> " 
		          << "error while reading uint32 from stream." << std::endl;
        return (long) ntohl( dataSizeNl );  
        
    }

    //---------------------------------------------------------------------------
    static long readFromStream(int sockfd, void *buffer, int dataSize, long *availableBytes) {

        char *data = (char *)buffer;
    
        if (availableBytes != NULL) *availableBytes = 0;
        
        // Read checkpoint data from the stream
        int nleft = dataSize;
        while ( nleft > 0 ) {
      
            int nread = read(sockfd, data, nleft);    
            if (nread <= 0) { // ERROR!!!
                std::cerr << "SocketUtils::readFromStream --> error reading from stream. " 
		                  << "Read " << dataSize - nleft + nread << " of "
		                  << dataSize << " bytes." << std::endl;
	           return dataSize - nleft + nread;
            }      

            nleft -= nread;
            data  += nread;
            
            if (availableBytes != NULL) *availableBytes += nread;
        }

        return dataSize;
    
    }

    //---------------------------------------------------------------------------
    static long readFromStreamToFile(int sockfd, int dataSize, int filefd) {

        int bufferSize = 1024; // 1MB
        char *data = new char[bufferSize];
    
        // Read checkpoint data from the stream
        int nleft = dataSize;
        while ( nleft > 0 ) {
      
            int nread = -1;
            if (nleft > bufferSize)
                nread = read(sockfd, data, bufferSize);
            else
                nread = read(sockfd, data, nleft);
                
            if (nread <= 0) { // ERROR!!!
                std::cerr << "SocketUtils::readFromStream --> error reading from stream. " 
                          << "Read " << dataSize - nleft + nread << " of "
                          << dataSize << " bytes." << std::endl;
               return dataSize - nleft + nread;
            }
            
            ssize_t nwritten = write(filefd, data, nread);      
            if (nwritten != nread)
                std::cerr << "SocketUtils::readFromStream --> error writing data to file." << std::endl;
            
            nleft -= nread;
        }

        delete data;
        
        return dataSize;    
    }
  
    //---------------------------------------------------------------------------
    static long writeToStream(int sockfd, const void *buffer, int dataSize) {

        char *data = (char *)buffer;
    
        // Writes checkpoint data to the stream
        int nleft = dataSize;
        while ( nleft > 0 ) {
      
            int nwritten = write(sockfd, data, nleft);    
            if (nwritten <= 0) { // ERROR!!!
                std::cerr << "SocketUtils::writeToStream --> error writing to stream. " 
		                  << "Wrote " << dataSize - nleft + nwritten << " of " 
		                  << dataSize << " bytes." << std::endl;
	           return dataSize - nleft + nwritten;
            }      
      
            nleft -= nwritten;
            data  += nwritten;
        }
    
        return dataSize;
    }

    //---------------------------------------------------------------------------
    static long writeToStreamInc(int sockfd, const void *buffer, int dataSize, long *availableBytes) {

        char *data = (char *)buffer;

        struct timespec sleepTime;
        sleepTime.tv_sec  = 0;
        sleepTime.tv_nsec = 100 * 1000 * 1000;
    
        // Writes checkpoint data to the stream
        int nTransferred = 0;
        while ( nTransferred < dataSize ) {
      
            while (*availableBytes <= nTransferred)
                nanosleep(&sleepTime, NULL);            
             
            int nwritten = write(sockfd, data, *availableBytes - nTransferred);    
            if (nwritten <= 0) { // ERROR!!!
                std::cerr << "SocketUtils::writeToStream --> error writing to stream. " 
                          << "Wrote " << nTransferred << " of " 
                          << dataSize << " bytes." << std::endl;
               return nTransferred;
            }      
      
            nTransferred += nwritten;
            data  += nwritten;
        }
            
        return dataSize;
    }

    //---------------------------------------------------------------------------
};

#endif
