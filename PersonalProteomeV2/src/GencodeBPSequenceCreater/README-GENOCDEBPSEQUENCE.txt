GencodeBPSequnece requires 3 parameters.  annotationFile  genomeDirectory  outputDirectory

I ran GencodeBPSequence successfully with the following command java -Xmx5G -jar GencodeBPSequence.jar /Users/davidthomas/Peppy/ProteomeV2/GencodeBPSequence/annotation/gencode.v11.chrm1.gtf /Users/davidthomas/Peppy/ProteomeV2/GencodeBPSequence/genome/hg19/ /Users/davidthomas/Peppy/ProteomeV2/GencodeBPSequence/output/


Information about the program.
 * GencodeBPSequence is a simple program that takes in a annotation and a genome and creates a fasta output file.  This output file conists of a series of header lines and a nucleotide sequence.
 * Each header is of the format:
 * 
 * >GeneName-TranscriptName|chr#|ProteinCoding: Y or N|Strand: + or -| Genetic Constructs (These can be UTR, EXON, or INTRON)
 * 
 * Some example lines:
 * >DDX11L1-DDX11L1-001|chr1|ProteinCoding: N|Strand: +|EXON1 141-188|INTRON1 189-309|EXON2 310-358|INTRON2 359-743|EXON3 744-828|INTRON3 829-1105|EXON4 1106-1183|INTRON4 1184-1351|EXON5 1352-1505|INTRON5 1506-1583|EXON6 1584-1801|
 * >FAM138A-FAM138A-001|chr1|ProteinCoding: Y|Strand: -|UTR1 0-344|EXON1 0-360|INTRON1 361-599|EXON2 600-804|INTRON2 805-906|EXON3 907-1527|UTR2 941-1527|
 * >OR4G11P-OR4G11P-001|chr1|ProteinCoding: N|Strand: +|EXON1 0-939|
 * >RNASEL-RNASEL-001|chr1|ProteinCoding: Y|Strand: -|UTR1 0-89|EXON1 0-89|UTR2 2286-2449|EXON2 2286-3929|INTRON1 3930-5089|EXON3 5090-5175|INTRON2 5176-6997|EXON4 6998-7203|INTRON3 7204-7898|EXON5 7899-8031|INTRON4 8032-12866|EXON6 12867-13000|INTRON5 13001-13677|EXON7 13678-15622|UTR3 13862-15622|
 *
 *Each set of header lines is followed by the DNA sequence for the preceding genes DNA.
 *
 *To save space, if several lines in a row come from the same gene, then those lines headers are stored back to back and only one sequence is printed.
 *
 *Ex.
 *>RNASEL-RNASEL-001|chr1|ProteinCoding: Y|Strand: -|UTR1 0-89|EXON1 0-89|UTR2 2286-2449|EXON2 2286-3929|INTRON1 3930-5089|EXON3 5090-5175|INTRON2 5176-6997|EXON4 6998-7203|INTRON3 7204-7898|EXON5 7899-8031|INTRON4 8032-12866|EXON6 12867-13000|INTRON5 13001-13677|EXON7 13678-15622|UTR3 13862-15622|
 *>RNASEL-RNASEL-002|chr1|ProteinCoding: Y|Strand: -|UTR1 0-89|EXON1 0-89|UTR2 2286-2449|EXON2 2286-3929|INTRON1 3930-5089|EXON3 5090-5175|INTRON2 5176-6997|EXON4 6998-7203|INTRON3 7204-7898|EXON5 7899-8031|INTRON4 8032-12866|EXON6 12867-13000|INTRON5 13001-13677|EXON7 13678-15622|UTR3 13862-15622|
 *Sequence
 *>OR4G11P-OR4G11P-001|chr1|ProteinCoding: N|Strand: +|EXON1 0-939|
 *Sequence
 *>FAM138A-FAM138A-001|chr1|ProteinCoding: Y|Strand: -|UTR1 0-344|EXON1 0-360|INTRON1 361-599|EXON2 600-804|INTRON2 805-906|EXON3 907-1527|UTR2 941-1527|
 *Sequence
 *
 *Usage:
 *
 *Gencode BP Sequence takes in three arguments in the following order:
 *
 *annotationFile - This is the path to a GENCODE gtf format annotation.  These can be had at: http://www.gencodegenes.org/
 *
 *genomeFile - This parameter is the directory where a series of fasta files named chr#.fa are located.  The reference human genome is  required for use with official GENCODE annotations
 *				and it can be found at: http://hgdownload.cse.ucsc.edu/downloads.html#human
 *
 *outputDirectory - This parameter simply states what folder the output file is to be placed in.  The output file will be named "SEQ_" + the original annotations name.  The file will be given
 *					the extension .fasta
 *
 *Requirements:
 *
 *GencodeBPSequence requires memory sufficient to hold the largest chromosome + the input annotations at any given time.  For humans with the complete gencode annotations around ~4GB is memory enough to ensure reasonable performance (More is better!).
 *Sets this with -Xmx4G as a parameter when launching this as a jar file.
 *
 * @author David "Corvette" Thomas June 2012