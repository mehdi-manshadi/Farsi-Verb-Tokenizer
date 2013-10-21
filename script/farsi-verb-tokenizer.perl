#!/usr/bin/perl

use File::Basename;

$dir  = dirname($0);
$script = basename($0);
die "Error! Missing command-line argument(s).\nUsage: $script [-d resource_folder] input_file [pattern_id]\n" if (@ARGV<1);

# parsing command-line params
$_ = shift @ARGV;
if (/-d/)
{
	die "Error! Missing command-line argument(s).\nUsage: $script [-d resource_folder] input_file [pattern_id]\n" if (@ARGV<2);
	$dir = shift @ARGV;
	$_ = shift @ARGV;
}

$fInput = $_;
$pattern_id = shift @ARGV if (@ARGV>0);

$fTransforms = $dir."/../resource/transformations.tsv";
$fStemPresent = $dir."/../resource/present-stems.txt";
$fStemPast = $dir."/../resource/past-stems.txt";

# Reading patterns
open(FT, '<', $fTransforms) || die "Error opening file $fTransforms!\n";
@transforms = <FT>;
close(FT);

# Reading present stems
open(FS, '<', $fStemPresent) || die "Error opening file $fStemPresent!\n";
$vbList = <FS>;
chomp $vbList;
while (<FS>)
{
	chomp;
	$vbList .= "|".$_;
}
close(FS);

# Reading past stems
open(FS, '<', $fStemPast) || die "Error opening file $fStemPast!\n";
$vbdList = <FS>;
chomp $vbdList;
while (<FS>)
{
	chomp;
	$vbdList .= "|".$_;
}
close(FS);

# Reading input sentences one by one and apply the transform(s) 
open(FI, '<', $fInput) || die "Error opening file $fInput!\n";
while (<FI>)
{
	for($i=0; $i<@transforms; $i++) # iterate through all patterns
	{
		next if (defined($pattern_id) && ($pattern_id!=$i)); # if pattern_number is defined
				# then only apply n_th pattern, where  n = pattern_number
		
		# extract search and replace patterns
		chomp $transforms[$i];
		@transform = split(/\t/, $transforms[$i]);
		$search  = $transform[0];
		$search =~ s/VBD/$vbdList/g;
		$search =~ s/VB/$vbList/g;
		$replace = $transform[1];
		$replace =~ s/\\/\$/g;
		$replace = '"'.$replace.'"';

		#print "$search\n$replace\n";
		# apply the tansformation
		s/$search/$replace/ee;		
	}
	
	print;
}
close(FI);