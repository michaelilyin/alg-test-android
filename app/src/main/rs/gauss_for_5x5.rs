#pragma version(1)
#pragma rs java_package_name(ru.michaelilyin.alg)

static const float k[25] = { 0.0030, 0.0133, 0.0219, 0.0133, 0.0030,
                            0.0133, 0.0596, 0.0983, 0.0596, 0.0133,
                            0.0219, 0.0983, 0.1621, 0.0983, 0.0219,
                            0.0133, 0.0596, 0.0983, 0.0596, 0.0133,
                            0.0030, 0.0133, 0.0219, 0.0133, 0.0030 };
                         	
rs_allocation in;
int size;

uchar4 __attribute__((kernel)) make_gauss(uint32_t x, uint32_t y) {
	float4 f4out = { 0, 0, 0, 0 };

    if (x < 5 || y < 5 || x > size - 6 || y > size - 6) {
        return convert_uchar4(f4out);
    }

	for (int kIdx = 0; kIdx < 25; kIdx++) {
		int kX = kIdx % 5 - 1;
		int kY = kIdx / 5 - 1;

        int xIdx = x + kX;
        int yIdx = y - kY;

        f4out += convert_float4(rsGetElementAt_uchar4(in, xIdx, yIdx)) * k[kIdx];
	}
	
	return convert_uchar4(f4out);
}