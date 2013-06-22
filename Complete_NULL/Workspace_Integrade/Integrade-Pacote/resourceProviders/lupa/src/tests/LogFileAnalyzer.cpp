//
// C++ Implementation: LogFileAnalyzer
//
// Description: 
//
//
// Author: Danilo Conde <danconde@danconde-laptop>, (C) 2007
//
// Copyright: See COPYING file that comes with this distribution
//
//

#include <sys/types.h>
#include <dirent.h>
#include <errno.h>
#include <vector>
#include <string>
#include <iostream>
#include <fstream>

#include "../DataParser.hpp"
#include "../KMeansClusteringAlgorithm.hpp"

using namespace std;

int getdir (string dir, vector<string> &files) {
	DIR *dp;
	struct dirent *dirp;
	if((dp = opendir(dir.c_str())) == NULL) {
		cout << "Error(" << errno << ") opening " << dir << endl;
		return errno;
	}
	
	while ((dirp = readdir(dp)) != NULL) {
		files.push_back(string(dirp->d_name));
	}
	closedir(dp);
	return 0;
}

int main(void) {
	char dirName[300] = "/home/danconde/coleta/";
	string dir = string(dirName);
	vector<string> files = vector<string>();
	KMeansClusteringAlgorithm *kmeans = new KMeansClusteringAlgorithm();
	vector<Cluster*> clusters;

	getdir(dir,files);
	cout << 	"Arquivo" << '\t' << "Dias" << '\t' << "Validos" << '\t' << "Percentagem" <<  endl;

	for (unsigned int i = 0;i < files.size();i++) {
		if (files[i].find(string(".log"), 0) != string::npos) {
			DataParser parser;
			vector<UsageData*> *usageDatas;
			vector<UsageData*> *validUsageDatas = new vector<UsageData*>();
			ofstream logDetails;
			Timestamp *t1, *t2;
			logDetails.open((string(dirName).append(string("analysis/analysis_")).append(files[i])).c_str());
			logDetails << "Arquivo" << '\t' << "Dias" << '\t' << "Validos" << '\t' << "Percentagem" <<  endl;
			
			t1 = new Timestamp();
			usageDatas = parser.parseFile((string(dirName).append(files[i])).c_str());
			t2 = new Timestamp();
			
			//**
			//*  PEGANDO SOMENTE DERIVATIVES DE USAGEDATAS VALIDOS
			//*/
			int validos = 0;
			for (unsigned int j = 0; j < usageDatas->size(); j++)
				if (usageDatas->at(j)->isValid()) {
					validUsageDatas->push_back(usageDatas->at(j)->derivative());
					validos++;
				}
			
			//logDetails << files[i] << '\t' << usageDatas->size() << '\t' << validos << '\t' << (100 * static_cast<float>(validos)/usageDatas->size()) <<  endl << endl;
			cout << files[i] << '\t' << usageDatas->size() << '\t' << validos << '\t' << (100 * static_cast<float>(validos)/usageDatas->size()) <<  '\t' << (t2->getRawTime() - t1->getRawTime()) << endl;
			vector<resource> resources = getAvailableResources();
			if ( validUsageDatas->size() > getK() ) {
				for ( unsigned int r = 0; r < resources.size(); r++ ) {
					  clusters = kmeans->analyzeData ( validUsageDatas, resources[r] );
					  unsigned int k;
					  for ( k = 0; k < clusters.size(); k++ )
					     logDetails << resources[r] << "_" << k << " (" << clusters[k]->getNumberOfElements() << ")\t";
					  logDetails << endl;
					  for (int l = 0; l < 2*SAMPLES_PER_DAY; l++ ) {
						for ( k = 0; k < clusters.size(); k++ )
						   logDetails << clusters[k]->getRepresentativeElement().getData()[l].getResource(resources[r]) << '\t';
						logDetails << endl;
				           }
					   logDetails << endl;
				}
			}
			
			logDetails.close();
			
		}
	}
	return 0;
}
