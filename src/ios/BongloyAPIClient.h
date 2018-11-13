//
//  BongloyAPIClient.h
//  bongloy-demo-ios
//
//  Created by khomsovon on 9/20/18.
//  Copyright © 2018 bongloy. All rights reserved.
//

#import <Stripe/Stripe.h>

@interface BongloyAPIClient : STPAPIClient

@property (nonatomic, strong, readwrite) NSURL *apiURL;
@property (nonatomic, strong, readonly) NSURLSession *urlSession;
@property (nonatomic, strong, readwrite) NSString *apiKey;

- (instancetype)initWithConfiguration:(STPPaymentConfiguration *)configuration NS_DESIGNATED_INITIALIZER;

@end


