#ifndef SRTP_TESTS_H
#define SRTP_TESTS_H

#ifdef __cplusplus
extern "C" {
#endif


extern int dtls_srtp_driver(void);

extern int rdbx_driver(unsigned do_timing_test, unsigned do_validation);

extern int replay_driver(int num_trials);

extern int roc_driver(int num_trials);

extern int rtpw(int argc, char *argv[]);

extern int srtp_driver(void);

extern int aes_calc(char *key, char *phrase, char *cipher);

extern int cipher_driver(unsigned do_timing_test, unsigned do_validation, unsigned do_array_timing_test);

extern int datatypes_driver(void);

extern int envTest(void);

extern int kernel_driver(unsigned do_validation, unsigned do_debug);

extern int rand_gen(unsigned num_octets, unsigned do_debug);

extern int sha1_driver(void);

extern int stat_driver(void);


#ifdef __cplusplus
}
#endif

#endif /* SRTP_TESTS_H */
