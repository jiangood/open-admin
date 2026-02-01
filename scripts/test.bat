

cd ../web
call pnpm version prerelease --preid=beta
call pnpm build
call pnpm publish -r --access public --no-git-checks --tag beta --registry https://packages.aliyun.com/62d39be70065edd3d51c1984/npm/npm-registry/



cd ../scripts


